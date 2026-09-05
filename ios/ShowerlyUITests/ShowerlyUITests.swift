import XCTest

@MainActor
final class ShowerlyUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testHomePagerAndBayNotice() {
        let app = launchApp()

        XCTAssertTrue(element("selectionSummary", in: app).waitForExistence(timeout: 3))
        XCTAssertTrue(element("bathroomPager", in: app).exists)
        XCTAssertTrue(app.staticTexts["1 / 2  ·  左右滑动切换浴室"].exists)

        app.buttons["crowdBall_31"].tap()
        XCTAssertTrue(app.alerts["浴位详情"].waitForExistence(timeout: 1))
        app.alerts["浴位详情"].buttons["知道了"].tap()
    }

    func testSettingsSelectionsPersistAcrossTabs() {
        let app = launchApp()
        XCTAssertTrue(element("selectionSummary", in: app).waitForExistence(timeout: 3))

        app.tabBars.buttons["设置"].tap()
        app.segmentedControls["genderPicker"].buttons["女"].tap()
        app.segmentedControls["campusPicker"].buttons["太白校区"].tap()
        app.segmentedControls["appearancePicker"].buttons["深色"].tap()

        app.tabBars.buttons["主页"].tap()
        XCTAssertTrue(app.staticTexts["女浴 · 太白校区 · 1 个浴室"].waitForExistence(timeout: 3))
    }

    func testPullToRefreshKeepsCurrentSelection() {
        let app = launchApp()
        XCTAssertTrue(element("selectionSummary", in: app).waitForExistence(timeout: 3))

        app.scrollViews.firstMatch.swipeDown()

        XCTAssertTrue(element("selectionSummary", in: app).waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["男浴 · 长安校区 · 2 个浴室"].exists)
    }

    func testErrorAndRetryState() {
        let app = launchApp(extraArguments: ["-FixtureError"])

        XCTAssertTrue(element("errorView", in: app).waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["retryButton"].exists)
        XCTAssertTrue(app.staticTexts["网络不可用，请检查连接后重试"].exists)

        app.buttons["retryButton"].tap()
        XCTAssertTrue(element("errorView", in: app).waitForExistence(timeout: 3))
    }

    private func launchApp(extraArguments: [String] = []) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments = ["-UITesting"] + extraArguments
        app.launch()
        return app
    }

    private func element(_ identifier: String, in app: XCUIApplication) -> XCUIElement {
        app.descendants(matching: .any)[identifier]
    }
}
