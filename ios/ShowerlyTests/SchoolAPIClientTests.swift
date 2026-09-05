import XCTest
@testable import Showerly

final class SchoolAPIClientTests: XCTestCase {
    func testRequestContainsCampusAndCompatibilityHeadersWithoutUID() throws {
        let date = Date(timeIntervalSince1970: 1_700_000_000)
        let request = SchoolAPIClient.makeRequest(
            campus: .taibai,
            forceRefresh: true,
            now: date,
            requestID: "fixture-id"
        )
        let components = try XCTUnwrap(URLComponents(url: try XCTUnwrap(request.url), resolvingAgainstBaseURL: false))

        XCTAssertEqual(components.queryItems, [URLQueryItem(name: "campusId", value: "36")])
        let changanRequest = SchoolAPIClient.makeRequest(
            campus: .changan,
            forceRefresh: false,
            now: date,
            requestID: "fixture-id"
        )
        let changanComponents = try XCTUnwrap(URLComponents(
            url: try XCTUnwrap(changanRequest.url),
            resolvingAgainstBaseURL: false
        ))
        XCTAssertEqual(changanComponents.queryItems, [URLQueryItem(name: "campusId", value: "4")])
        XCTAssertFalse(request.url?.absoluteString.contains("uid=") == true)
        XCTAssertEqual(request.value(forHTTPHeaderField: "timestamp"), "1700000000000")
        XCTAssertEqual(request.value(forHTTPHeaderField: "requestid"), "1700000000000-fixture-id")
        XCTAssertEqual(request.value(forHTTPHeaderField: "os"), "android")
        XCTAssertEqual(request.value(forHTTPHeaderField: "versionno"), "120")
        XCTAssertEqual(request.value(forHTTPHeaderField: "user-agent"), "okhttp-okgo/jeasonlzy")
        XCTAssertEqual(request.value(forHTTPHeaderField: "Cache-Control"), "no-cache")
        XCTAssertNil(request.value(forHTTPHeaderField: "Authorization"))
    }

    func testCacheIsReusedAndForceRefreshBypassesIt() async throws {
        let counter = RequestCounter()
        let payload = Data(#"{"code":"200","data":[{"id":1,"name":"A","sex":0,"maxLoad":10,"useCount":2}]}"#.utf8)
        let response = HTTPURLResponse(
            url: SchoolAPIClient.endpoint,
            statusCode: 200,
            httpVersion: nil,
            headerFields: nil
        )!
        let client = SchoolAPIClient(now: { Date(timeIntervalSince1970: 100) }) { _ in
            await counter.increment()
            return (payload, response)
        }

        _ = try await client.fetchBathrooms(campus: .changan, forceRefresh: false)
        _ = try await client.fetchBathrooms(campus: .changan, forceRefresh: false)
        var count = await counter.count
        XCTAssertEqual(count, 1)

        _ = try await client.fetchBathrooms(campus: .changan, forceRefresh: true)
        count = await counter.count
        XCTAssertEqual(count, 2)
    }

    func testResponseWithoutDataIsClassified() async {
        let payload = Data(#"{"code":"200","msg":"成功"}"#.utf8)
        let response = HTTPURLResponse(url: SchoolAPIClient.endpoint, statusCode: 200, httpVersion: nil, headerFields: nil)!
        let client = SchoolAPIClient(now: { Date() }) { _ in (payload, response) }

        do {
            _ = try await client.fetchBathrooms(campus: .changan, forceRefresh: false)
            XCTFail("Expected noData")
        } catch {
            XCTAssertEqual(error as? SchoolAPIError, .noData)
        }
    }

    func testTimeoutIsClassified() async {
        let client = SchoolAPIClient(now: { Date() }) { _ in
            throw URLError(.timedOut)
        }

        do {
            _ = try await client.fetchBathrooms(campus: .changan, forceRefresh: false)
            XCTFail("Expected timedOut")
        } catch {
            XCTAssertEqual(error as? SchoolAPIError, .timedOut)
        }
    }

    func testCancellationPropagatesAsCancellationError() async {
        let payload = Data(#"{"code":"200","data":[]}"#.utf8)
        let response = HTTPURLResponse(
            url: SchoolAPIClient.endpoint,
            statusCode: 200,
            httpVersion: nil,
            headerFields: nil
        )!
        let client = SchoolAPIClient(now: { Date() }) { _ in
            try await Task.sleep(for: .seconds(60))
            return (payload, response)
        }
        let task = Task {
            try await client.fetchBathrooms(campus: .changan, forceRefresh: false)
        }

        await Task.yield()
        task.cancel()

        do {
            _ = try await task.value
            XCTFail("Expected cancellation")
        } catch is CancellationError {
            // Expected: cancellation is not converted into a user-facing network error.
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }
}

private actor RequestCounter {
    private(set) var count = 0
    func increment() { count += 1 }
}
