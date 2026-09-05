import Foundation
import Observation

@Observable
@MainActor
final class HomeViewModel {
    private let client: any SchoolAPIClientProtocol
    private let now: () -> Date
    private var currentTask: Task<Void, Never>?

    private(set) var isLoading = true
    private(set) var bathrooms: [BathroomStatus] = []
    private(set) var gender: Gender = .male
    private(set) var campus: Campus = .changan
    private(set) var timeText = ""
    private(set) var errorMessage: String?

    init(
        client: any SchoolAPIClientProtocol,
        now: @escaping () -> Date = Date.init
    ) {
        self.client = client
        self.now = now
    }

    func load(settings: AppSettings, forceRefresh: Bool) async {
        currentTask?.cancel()
        let task = Task { [weak self] in
            guard let self else { return }
            await self.performLoad(settings: settings, forceRefresh: forceRefresh)
        }
        currentTask = task

        await withTaskCancellationHandler {
            await task.value
        } onCancel: {
            task.cancel()
        }
    }

    func cancel() {
        currentTask?.cancel()
    }

    private func performLoad(settings: AppSettings, forceRefresh: Bool) async {
        let selectionChanged = gender != settings.gender || campus != settings.campus
        gender = settings.gender
        campus = settings.campus
        isLoading = true
        errorMessage = nil
        if selectionChanged {
            bathrooms = []
            timeText = ""
        }

        do {
            let source = try await client.fetchBathrooms(
                campus: settings.campus,
                forceRefresh: forceRefresh
            )
            try Task.checkCancellation()

            let filtered = source.filter { $0.sex == settings.gender.sex }
            bathrooms = filtered
            timeText = Self.timeFormatter.string(from: now())
            errorMessage = filtered.isEmpty ? "当前筛选下暂无浴室" : nil
            isLoading = false
        } catch is CancellationError {
            return
        } catch {
            isLoading = false
            errorMessage = (error as? LocalizedError)?.errorDescription ?? "刷新失败，请稍后重试"
        }
    }

    private static let timeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = .autoupdatingCurrent
        formatter.dateFormat = "HH:mm"
        return formatter
    }()
}
