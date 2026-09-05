import Foundation

protocol SchoolAPIClientProtocol: Sendable {
    func fetchBathrooms(campus: Campus, forceRefresh: Bool) async throws -> [BathroomStatus]
}

enum SchoolAPIError: Error, Equatable, LocalizedError, Sendable {
    case offline
    case timedOut
    case http(Int)
    case invalidResponse(String?)
    case noData

    var errorDescription: String? {
        switch self {
        case .offline: "网络不可用，请检查连接后重试"
        case .timedOut: "连接超时，请稍后重试"
        case .http(let status): "服务暂时不可用（HTTP \(status)）"
        case .invalidResponse(let message): message?.isEmpty == false ? message : "服务返回的数据无法读取"
        case .noData: "服务未返回浴室数据"
        }
    }
}

actor SchoolAPIClient: SchoolAPIClientProtocol {
    typealias Loader = @Sendable (URLRequest) async throws -> (Data, URLResponse)

    private struct CacheEntry: Sendable {
        let timestamp: Date
        let bathrooms: [BathroomStatus]
    }

    static let endpoint = URL(string: "https://cloudman.jinghaojian.net/bathroom")!
    private static let cacheLifetime: TimeInterval = 15

    private let loader: Loader
    private let now: @Sendable () -> Date
    private var cache: [Campus: CacheEntry] = [:]

    init(session: URLSession = SchoolAPIClient.makeSession()) {
        loader = { request in
            try await session.data(for: request)
        }
        now = { Date() }
    }

    init(
        now: @escaping @Sendable () -> Date,
        loader: @escaping Loader
    ) {
        self.now = now
        self.loader = loader
    }

    func fetchBathrooms(campus: Campus, forceRefresh: Bool) async throws -> [BathroomStatus] {
        let currentDate = now()
        if !forceRefresh,
           let entry = cache[campus],
           currentDate.timeIntervalSince(entry.timestamp) < Self.cacheLifetime {
            return entry.bathrooms
        }

        let request = Self.makeRequest(
            campus: campus,
            forceRefresh: forceRefresh,
            now: currentDate,
            requestID: UUID().uuidString
        )

        do {
            let (data, response) = try await loader(request)
            try Task.checkCancellation()

            guard let http = response as? HTTPURLResponse else {
                throw SchoolAPIError.invalidResponse(nil)
            }
            guard (200..<300).contains(http.statusCode) else {
                throw SchoolAPIError.http(http.statusCode)
            }

            let payload: CrowdAPIResponse
            do {
                payload = try JSONDecoder().decode(CrowdAPIResponse.self, from: data)
            } catch {
                throw SchoolAPIError.invalidResponse(nil)
            }

            if let code = payload.code, code != "200" {
                throw SchoolAPIError.invalidResponse(payload.message)
            }
            guard let source = payload.data else {
                throw SchoolAPIError.noData
            }

            let bathrooms = source.map { $0.toStatus() }
            cache[campus] = CacheEntry(timestamp: currentDate, bathrooms: bathrooms)
            return bathrooms
        } catch is CancellationError {
            throw CancellationError()
        } catch let error as SchoolAPIError {
            throw error
        } catch let error as URLError {
            switch error.code {
            case .cancelled: throw CancellationError()
            case .timedOut: throw SchoolAPIError.timedOut
            case .notConnectedToInternet, .networkConnectionLost, .cannotFindHost, .cannotConnectToHost:
                throw SchoolAPIError.offline
            default:
                throw SchoolAPIError.invalidResponse(error.localizedDescription)
            }
        } catch {
            throw SchoolAPIError.invalidResponse(error.localizedDescription)
        }
    }

    static func makeRequest(
        campus: Campus,
        forceRefresh: Bool,
        now: Date,
        requestID: String
    ) -> URLRequest {
        var components = URLComponents(url: endpoint, resolvingAgainstBaseURL: false)!
        components.queryItems = [URLQueryItem(name: "campusId", value: campus.campusID)]

        var request = URLRequest(url: components.url!, timeoutInterval: 20)
        request.httpMethod = "GET"
        let timestamp = String(Int64(now.timeIntervalSince1970 * 1_000))
        request.setValue("application/json", forHTTPHeaderField: "accept")
        request.setValue("zh-CN,zh;q=0.8", forHTTPHeaderField: "accept-language")
        request.setValue("okhttp-okgo/jeasonlzy", forHTTPHeaderField: "user-agent")
        request.setValue(timestamp, forHTTPHeaderField: "timestamp")
        request.setValue("\(timestamp)-\(requestID)", forHTTPHeaderField: "requestid")
        request.setValue("android", forHTTPHeaderField: "os")
        request.setValue("120", forHTTPHeaderField: "versionno")
        request.setValue(forceRefresh ? "no-cache" : "max-age=15", forHTTPHeaderField: "Cache-Control")
        return request
    }

    private static func makeSession() -> URLSession {
        let configuration = URLSessionConfiguration.ephemeral
        configuration.timeoutIntervalForRequest = 20
        configuration.timeoutIntervalForResource = 20
        configuration.requestCachePolicy = .reloadIgnoringLocalCacheData
        return URLSession(configuration: configuration)
    }
}

#if DEBUG
actor FixtureSchoolAPIClient: SchoolAPIClientProtocol {
    enum Mode: Sendable {
        case success
        case error
    }

    let mode: Mode

    init(mode: Mode) {
        self.mode = mode
    }

    func fetchBathrooms(campus: Campus, forceRefresh: Bool) async throws -> [BathroomStatus] {
        try await Task.sleep(for: .milliseconds(120))
        if mode == .error {
            throw SchoolAPIError.offline
        }
        return [
            BathroomStatus(id: 31, name: "博硕2楼男", sex: 0, maxLoad: 62, useCount: 3, vacant: 59, capacity: 62, occupancyRatio: 3.0 / 62.0, statusLabel: "正常"),
            BathroomStatus(id: 46, name: "东区第一浴室", sex: 0, maxLoad: 20, useCount: 18, vacant: 2, capacity: 20, occupancyRatio: 0.9, statusLabel: "爆满"),
            BathroomStatus(id: 81, name: "太白女生浴室", sex: 1, maxLoad: 30, useCount: 12, vacant: 18, capacity: 30, occupancyRatio: 0.4, statusLabel: "正常")
        ]
    }
}
#endif
