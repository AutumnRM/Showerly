import XCTest
@testable import Showerly

@MainActor
final class HomeViewModelTests: XCTestCase {
    func testFiltersByGenderAndFormatsUpdateTime() async {
        let client = ScriptedClient(results: [.success(Self.fixtureBathrooms)])
        let model = HomeViewModel(
            client: client,
            now: { Date(timeIntervalSince1970: 0) }
        )

        await model.load(
            settings: AppSettings(gender: .female, campus: .changan),
            forceRefresh: false
        )

        XCTAssertEqual(model.bathrooms.map(\.name), ["女浴室"])
        XCTAssertFalse(model.isLoading)
        XCTAssertNil(model.errorMessage)
    }

    func testFailureKeepsExistingDataForSameSelection() async {
        let client = ScriptedClient(results: [
            .success(Self.fixtureBathrooms),
            .failure(.offline)
        ])
        let model = HomeViewModel(client: client)
        let settings = AppSettings(gender: .male, campus: .changan)

        await model.load(settings: settings, forceRefresh: false)
        let previous = model.bathrooms
        await model.load(settings: settings, forceRefresh: true)

        XCTAssertEqual(model.bathrooms, previous)
        XCTAssertEqual(model.errorMessage, SchoolAPIError.offline.errorDescription)
    }

    func testSelectionChangeClearsOldDataOnFailure() async {
        let client = ScriptedClient(results: [
            .success(Self.fixtureBathrooms),
            .failure(.offline)
        ])
        let model = HomeViewModel(client: client)

        await model.load(settings: AppSettings(gender: .male, campus: .changan), forceRefresh: false)
        await model.load(settings: AppSettings(gender: .female, campus: .taibai), forceRefresh: false)

        XCTAssertTrue(model.bathrooms.isEmpty)
        XCTAssertEqual(model.errorMessage, SchoolAPIError.offline.errorDescription)
    }

    private static let fixtureBathrooms = [
        BathroomStatus(id: 1, name: "男浴室", sex: 0, maxLoad: 10, useCount: 1, vacant: 9, capacity: 10, occupancyRatio: 0.1, statusLabel: "正常"),
        BathroomStatus(id: 2, name: "女浴室", sex: 1, maxLoad: 10, useCount: 2, vacant: 8, capacity: 10, occupancyRatio: 0.2, statusLabel: "正常")
    ]
}
private actor ScriptedClient: SchoolAPIClientProtocol {
    private var results: [Result<[BathroomStatus], SchoolAPIError>]

    init(results: [Result<[BathroomStatus], SchoolAPIError>]) {
        self.results = results
    }

    func fetchBathrooms(campus: Campus, forceRefresh: Bool) async throws -> [BathroomStatus] {
        guard !results.isEmpty else { throw SchoolAPIError.noData }
        return try results.removeFirst().get()
    }
}
