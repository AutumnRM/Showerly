import SwiftUI

struct SettingsView: View {
    @Environment(SettingsStore.self) private var settings

    var body: some View {
        @Bindable var settings = settings

        NavigationStack {
            ScrollView {
                GlassEffectContainer(spacing: 20) {
                    VStack(spacing: 20) {
                        SettingsSection(title: "性别", systemImage: "person.2.fill") {
                            Picker("性别", selection: $settings.gender) {
                                ForEach(Gender.allCases) { gender in
                                    Text(gender.label).tag(gender)
                                }
                            }
                            .pickerStyle(.segmented)
                            .accessibilityIdentifier("genderPicker")
                        }

                        SettingsSection(title: "校区", systemImage: "building.columns.fill") {
                            Picker("校区", selection: $settings.campus) {
                                ForEach(Campus.allCases) { campus in
                                    Text(campus.label).tag(campus)
                                }
                            }
                            .pickerStyle(.segmented)
                            .accessibilityIdentifier("campusPicker")
                        }

                        SettingsSection(title: "深色模式", systemImage: "circle.lefthalf.filled") {
                            Picker("深色模式", selection: $settings.appearance) {
                                ForEach(AppearancePreference.allCases) { appearance in
                                    Text(appearance.label).tag(appearance)
                                }
                            }
                            .pickerStyle(.segmented)
                            .accessibilityIdentifier("appearancePicker")
                        }

                        Label("偏好会自动保存，性别或校区变化后主页会立即刷新。", systemImage: "checkmark.icloud.fill")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 6)
                    }
                    .padding(20)
                }
            }
            .contentMargins(.horizontal, 18, for: .scrollContent)
            .contentMargins(.vertical, 20, for: .scrollContent)
            .navigationTitle("设置")
        }
    }
}

private struct SettingsSection<Content: View>: View {
    let title: String
    let systemImage: String
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Label(title, systemImage: systemImage)
                .font(.headline)
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(18)
        .glassEffect(.regular.interactive(), in: .rect(cornerRadius: 22))
    }
}
