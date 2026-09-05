import XCTest
@testable import Showerly

final class AppModelsTests: XCTestCase {
    func testDecodingIgnoresUnknownFieldsAndAllowsMissingValues() throws {
        let json = Data(#"{"code":"200","msg":"成功","unknown":true,"data":[{"id":31,"name":"博硕2楼男","sex":0,"maxLoad":62,"useCount":3,"futureField":"ok"},{"id":99}]}"#.utf8)
        let response = try JSONDecoder().decode(CrowdAPIResponse.self, from: json)

        XCTAssertEqual(response.code, "200")
        XCTAssertEqual(response.data?.count, 2)
        XCTAssertNil(response.data?[1].maxLoad)
    }

    func testOverCapacityUsesCurrentCountAsCapacity() {
        let status = makeDTO(maxLoad: 5, useCount: 8).toStatus()

        XCTAssertEqual(status.capacity, 8)
        XCTAssertEqual(status.vacant, 0)
        XCTAssertEqual(status.occupancyRatio, 1)
        XCTAssertEqual(status.statusLabel, "爆满")
    }

    func testStatusThresholds() {
        XCTAssertEqual(makeDTO(maxLoad: 10, useCount: 0).toStatus().statusLabel, "空闲")
        XCTAssertEqual(makeDTO(maxLoad: 10, useCount: 1).toStatus().statusLabel, "正常")
        XCTAssertEqual(makeDTO(maxLoad: 10, useCount: 6).toStatus().statusLabel, "较拥挤")
        XCTAssertEqual(makeDTO(maxLoad: 10, useCount: 9).toStatus().statusLabel, "爆满")
        XCTAssertEqual(makeDTO(maxLoad: 0, useCount: 0).toStatus().statusLabel, "未知")
    }

    private func makeDTO(maxLoad: Int, useCount: Int) -> BathroomDTO {
        BathroomDTO(
            id: 1,
            name: "测试浴室",
            sex: 0,
            maxLoad: maxLoad,
            useCount: useCount,
            bookingDeviceCount: nil,
            availableBookingDeviceCount: nil
        )
    }
}
