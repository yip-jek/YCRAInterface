
// 地市信息
public class YCIRegion {

	private String m_cityCode = null;			// 地市编码
	private String m_cityName = null;			// 地市名称（中文）

	public YCIRegion(String city_code, String city_name) {
		m_cityCode = city_code;
		m_cityName = city_name;
	}

	// 获取地市编码
	public String GetCityCode() {
		return m_cityCode;
	}

	// 获取地市名称（中文）
	public String GetCityName() {
		return m_cityName;
	}

}
