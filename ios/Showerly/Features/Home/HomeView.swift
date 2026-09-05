import SwiftUI

struct HomeView: View {
    @Environment(SettingsStore.self) private var settings
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var model: HomeViewModel
    @State private var selectedBathroomID: Int?
    @State private var showsBayNotice = false

    init(client: any SchoolAPIClientProtocol) {
        _model = State(initialValue: HomeViewModel(client: client))
    }

    var body: some View {
        NavigationStack {
            content
                .navigationTitle("Showerly")
                .toolbar {
                    ToolbarItem(placement: .primaryAction) {
                        Button {
                            Task { await model.load(settings: settings.snapshot, forceRefresh: true) }
                        } label: {
                            if model.isLoading {
                                ProgressView()
                            } else {
                                Label("刷新", systemImage: "arrow.clockwise")
                            }
                        }
                        .disabled(model.isLoading)
                        .accessibilityIdentifier("refreshButton")
                    }
                }
                .task(id: settings.requestSelection) {
                    await model.load(settings: settings.snapshot, forceRefresh: false)
                }
                .onDisappear { model.cancel() }
                .onChange(of: model.bathrooms) { _, bathrooms in
                    if selectedBathroomID == nil || !bathrooms.contains(where: { $0.id == selectedBathroomID }) {
                        selectedBathroomID = bathrooms.first?.id
                    }
                }
                .alert("浴位详情", isPresented: $showsBayNotice) {
                    Button("知道了", role: .cancel) {}
                } message: {
                    Text("浴位示意图尚未接入，敬请期待")
                }
        }
    }

    @ViewBuilder
    private var content: some View {
        if model.bathrooms.isEmpty {
            ScrollView {
                emptyState
                    .frame(maxWidth: .infinity, minHeight: 520)
            }
            .refreshable { await model.load(settings: settings.snapshot, forceRefresh: true) }
        } else {
            ScrollView(.vertical) {
                VStack(spacing: 14) {
                    selectionSummary
                    if let error = model.errorMessage {
                        InlineErrorView(message: error, timeText: model.timeText)
                    }
                    bathroomPager
                    pageHint
                }
                .padding(.vertical, 8)
            }
            .refreshable { await model.load(settings: settings.snapshot, forceRefresh: true) }
        }
    }

    private var emptyState: some View {
        VStack(spacing: 18) {
            if model.isLoading {
                ProgressView()
                    .controlSize(.large)
                Text("正在获取浴室状态…")
                    .foregroundStyle(.secondary)
            } else {
                Image(systemName: "wifi.exclamationmark")
                    .font(.system(size: 42))
                    .foregroundStyle(.secondary)
                Text(model.errorMessage ?? "当前筛选下暂无浴室")
                    .font(.headline)
                    .multilineTextAlignment(.center)
                Button("重试") {
                    Task { await model.load(settings: settings.snapshot, forceRefresh: true) }
                }
                .buttonStyle(.glassProminent)
                .accessibilityIdentifier("retryButton")
            }
        }
        .padding(28)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(model.isLoading ? "loadingView" : "errorView")
    }

    private var selectionSummary: some View {
        HStack(spacing: 6) {
            Image(systemName: model.gender == .male ? "figure.stand" : "figure.dress.line.vertical.figure")
            Text("\(model.gender.label)浴 · \(model.campus.label) · \(model.bathrooms.count) 个浴室")
                .lineLimit(1)
                .minimumScaleFactor(0.8)
        }
        .font(.subheadline.weight(.medium))
        .foregroundStyle(.secondary)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 24)
        .accessibilityIdentifier("selectionSummary")
    }

    private var bathroomPager: some View {
        ScrollView(.horizontal) {
            LazyHStack(spacing: 0) {
                ForEach(model.bathrooms) { bathroom in
                    BathroomCard(
                        bathroom: bathroom,
                        timeText: model.timeText,
                        reduceMotion: reduceMotion,
                        onOpenBays: { showsBayNotice = true }
                    )
                    .containerRelativeFrame(.horizontal)
                    .id(bathroom.id)
                }
            }
            .scrollTargetLayout()
        }
        .scrollIndicators(.hidden)
        .scrollTargetBehavior(.paging)
        .scrollPosition(id: $selectedBathroomID)
        .frame(minHeight: 470)
        .accessibilityIdentifier("bathroomPager")
    }

    private var pageHint: some View {
        let index = model.bathrooms.firstIndex(where: { $0.id == selectedBathroomID }).map { $0 + 1 } ?? 1
        return Text("\(index) / \(model.bathrooms.count)  ·  左右滑动切换浴室")
            .font(.footnote)
            .foregroundStyle(.secondary)
            .frame(maxWidth: .infinity)
            .accessibilityIdentifier("pageHint")
    }
}

private struct BathroomCard: View {
    let bathroom: BathroomStatus
    let timeText: String
    let reduceMotion: Bool
    let onOpenBays: () -> Void

    var body: some View {
        GlassEffectContainer(spacing: 14) {
            VStack(spacing: 14) {
                HStack(alignment: .firstTextBaseline) {
                    Text(bathroom.name)
                        .font(.title2.bold())
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Text(bathroom.statusLabel)
                        .font(.caption.bold())
                        .padding(.horizontal, 12)
                        .padding(.vertical, 7)
                        .glassEffect(.regular, in: .capsule)
                }

                BreathingBall(bathroom: bathroom, reduceMotion: reduceMotion, action: onOpenBays)

                VStack(spacing: 4) {
                    Text("\(bathroom.useCount) 人在洗")
                        .font(.title3.weight(.semibold))
                    Text("空位 \(bathroom.vacant) / 容量 \(bathroom.capacity)")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }

                ProgressView(value: bathroom.occupancyRatio)
                    .tint(ShowerlyStyle.crowdColor(bathroom.occupancyRatio))
                    .scaleEffect(x: 1, y: 1.6)

                VStack(alignment: .leading, spacing: 4) {
                    Text(bathroom.advice.title)
                        .font(.subheadline.weight(.semibold))
                    Text(bathroom.advice.detail)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(14)
                .glassEffect(.regular, in: .rect(cornerRadius: 16))

                Text("更新于 \(timeText)")
                    .font(.caption)
                    .foregroundStyle(.tertiary)
            }
            .padding(22)
            .frame(maxWidth: 620)
            .showerlyGlassCard()
            .padding(.horizontal, 24)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("bathroomCard_\(bathroom.id)")
    }
}

private struct BreathingBall: View {
    let bathroom: BathroomStatus
    let reduceMotion: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            TimelineView(.animation(minimumInterval: 1 / 30, paused: reduceMotion)) { timeline in
                let elapsed = timeline.date.timeIntervalSinceReferenceDate
                let scale = reduceMotion ? 1 : 1 + sin(elapsed * .pi / 1.5) * 0.06
                ZStack {
                    Circle()
                        .fill(ShowerlyStyle.crowdColor(bathroom.occupancyRatio))
                    Canvas { context, size in
                        drawParticles(in: &context, size: size, elapsed: elapsed)
                    }
                }
                .scaleEffect(scale)
            }
            .frame(width: 112, height: 112)
        }
        .buttonStyle(.plain)
        .glassEffect(.regular.interactive(), in: .circle)
        .accessibilityLabel("\(bathroom.name)，\(bathroom.useCount) 人在洗，空位 \(bathroom.vacant)，查看浴位详情")
        .accessibilityIdentifier("crowdBall_\(bathroom.id)")
    }

    private func drawParticles(in context: inout GraphicsContext, size: CGSize, elapsed: TimeInterval) {
        let count = bathroom.useCount == 0 ? 0 : max(1, Int((bathroom.occupancyRatio * 24).rounded())) + 1
        var generator = SeededGenerator(seed: UInt64(abs(bathroom.id) &* 31 &+ count))
        let center = CGPoint(x: size.width / 2, y: size.height / 2)
        let orbit = min(size.width, size.height) / 2

        for index in 0..<count {
            let phase = generator.nextUnit() * 2 * .pi
            let distance = (0.08 + generator.nextUnit() * 0.78) * orbit
            let radius = 1.5 + generator.nextUnit() * 2.5
            let alpha = 0.35 + generator.nextUnit() * 0.45
            let angle = phase + elapsed * 2 * .pi / 9
            let twinkle = reduceMotion ? 1 : 0.75 + 0.25 * sin(elapsed * 2 + Double(index) * .pi / 2)
            let point = CGPoint(x: center.x + cos(angle) * distance, y: center.y + sin(angle) * distance)
            context.fill(
                Path(ellipseIn: CGRect(x: point.x - radius, y: point.y - radius, width: radius * 2, height: radius * 2)),
                with: .color(.white.opacity(alpha * twinkle))
            )
        }
    }
}

private struct SeededGenerator {
    private var state: UInt64

    init(seed: UInt64) {
        state = seed == 0 ? 0x9E3779B97F4A7C15 : seed
    }

    mutating func nextUnit() -> Double {
        state = state &* 6364136223846793005 &+ 1442695040888963407
        return Double(state >> 11) / Double(1 << 53)
    }
}

private struct InlineErrorView: View {
    let message: String
    let timeText: String

    var body: some View {
        Label {
            Text(timeText.isEmpty ? message : "\(message) · 正在显示 \(timeText) 的数据")
        } icon: {
            Image(systemName: "exclamationmark.triangle.fill")
        }
        .font(.footnote)
        .foregroundStyle(.red)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .glassEffect(.regular, in: .rect(cornerRadius: 16))
        .padding(.horizontal, 24)
        .accessibilityIdentifier("inlineError")
    }
}
