import SwiftUI

@main
struct ShowerlyApp: App {
    @State private var settings: SettingsStore
    private let client: any SchoolAPIClientProtocol

    init() {
        let arguments = ProcessInfo.processInfo.arguments
        _settings = State(initialValue: SettingsStore(reset: arguments.contains("-UITesting")))
#if DEBUG
        if arguments.contains("-UITesting") {
            let mode: FixtureSchoolAPIClient.Mode = arguments.contains("-FixtureError") ? .error : .success
            client = FixtureSchoolAPIClient(mode: mode)
        } else {
            client = SchoolAPIClient()
        }
#else
        client = SchoolAPIClient()
#endif
    }

    var body: some Scene {
        WindowGroup {
            RootView(client: client)
                .environment(settings)
                .preferredColorScheme(settings.appearance.colorScheme)
        }
    }
}

private struct RootView: View {
    let client: any SchoolAPIClientProtocol

    var body: some View {
        TabView {
            Tab("主页", systemImage: "house.fill") {
                HomeView(client: client)
            }
            .accessibilityIdentifier("homeTab")

            Tab("设置", systemImage: "gearshape.fill") {
                SettingsView()
            }
            .accessibilityIdentifier("settingsTab")
        }
    }
}
