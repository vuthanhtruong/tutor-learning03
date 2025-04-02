const countrySelect = document.getElementById("country");
const provinceSelect = document.getElementById("province");
const districtSelect = document.getElementById("district");
const wardSelect = document.getElementById("ward");

// Tải danh sách quốc gia từ Rest Countries API
axios.get("https://restcountries.com/v3.1/all")
    .then(response => {
        const countries = response.data.sort((a, b) => a.name.common.localeCompare(b.name.common));
        countries.forEach(country => {
            const option = document.createElement("option");
            option.value = country.cca2;
            option.textContent = country.name.common;
            countrySelect.appendChild(option);
        });
    })
    .catch(error => {
        console.error("Lỗi tải quốc gia:", error);
        countrySelect.innerHTML = '<option value="">Không thể tải quốc gia</option>';
    });

// Tải danh sách tỉnh/thành phố khi chọn quốc gia
countrySelect.addEventListener("change", function () {
    const countryCode = this.value;
    provinceSelect.innerHTML = '<option value="">Chọn tỉnh/thành phố</option>';
    districtSelect.innerHTML = '<option value="">Chọn quận/huyện</option>';
    wardSelect.innerHTML = '<option value="">Chọn xã/phường</option>';

    if (countryCode) {
        const geonameId = getCountryGeonameId(countryCode);
        if (!geonameId) {
            provinceSelect.innerHTML = '<option value="">Quốc gia này chưa được hỗ trợ</option>';
            return;
        }

        const geoNamesUsername = "myusername"; // Thay bằng username GeoNames của bạn
        const url = `http://api.geonames.org/childrenJSON?geonameId=${geonameId}&username=${geoNamesUsername}`;
        console.log("Gửi yêu cầu tới:", url);

        axios.get(url)
            .then(response => {
                console.log("Dữ liệu trả về:", response.data);
                const provinces = response.data.geonames || [];
                if (!provinces || provinces.length === 0) {
                    provinceSelect.innerHTML = '<option value="">Không có dữ liệu tỉnh/thành phố</option>';
                } else {
                    provinces.forEach(province => {
                        const option = document.createElement("option");
                        option.value = province.name;
                        option.textContent = province.name;
                        option.dataset.geonameId = province.geonameId;
                        provinceSelect.appendChild(option);
                    });
                }
            })
            .catch(error => {
                const errorMessage = error.response
                    ? (error.response.data.status ? error.response.data.status.message : error.response.data)
                    : error.message;
                console.error("Lỗi tải tỉnh/thành phố:", errorMessage);
                provinceSelect.innerHTML = `<option value="">Lỗi: ${errorMessage}</option>`;
            });
    }
});

// Tải danh sách quận/huyện khi chọn tỉnh/thành phố
provinceSelect.addEventListener("change", function () {
    const selectedOption = this.options[this.selectedIndex];
    const provinceGeonameId = selectedOption ? selectedOption.dataset.geonameId : null;
    districtSelect.innerHTML = '<option value="">Chọn quận/huyện</option>';
    wardSelect.innerHTML = '<option value="">Chọn xã/phường</option>';

    if (provinceGeonameId) {
        const geoNamesUsername = "myusername"; // Thay bằng username GeoNames của bạn
        const url = `http://api.geonames.org/childrenJSON?geonameId=${provinceGeonameId}&username=${geoNamesUsername}`;
        console.log("Gửi yêu cầu tới:", url);

        axios.get(url)
            .then(response => {
                console.log("Dữ liệu trả về:", response.data);
                const districts = response.data.geonames || [];
                if (!districts || districts.length === 0) {
                    districtSelect.innerHTML = '<option value="">Không có dữ liệu quận/huyện</option>';
                } else {
                    districts.forEach(district => {
                        const option = document.createElement("option");
                        option.value = district.name;
                        option.textContent = district.name;
                        option.dataset.geonameId = district.geonameId;
                        districtSelect.appendChild(option);
                    });
                }
            })
            .catch(error => {
                const errorMessage = error.response
                    ? (error.response.data.status ? error.response.data.status.message : error.response.data)
                    : error.message;
                console.error("Lỗi tải quận/huyện:", errorMessage);
                districtSelect.innerHTML = `<option value="">Lỗi: ${errorMessage}</option>`;
            });
    }
});

// Tải danh sách xã/phường khi chọn quận/huyện
districtSelect.addEventListener("change", function () {
    const selectedOption = this.options[this.selectedIndex];
    const districtGeonameId = selectedOption ? selectedOption.dataset.geonameId : null;
    wardSelect.innerHTML = '<option value="">Chọn xã/phường</option>';

    if (districtGeonameId) {
        const geoNamesUsername = "myusername"; // Thay bằng username GeoNames của bạn
        const url = `http://api.geonames.org/childrenJSON?geonameId=${districtGeonameId}&username=${geoNamesUsername}`;
        console.log("Gửi yêu cầu tới:", url);

        axios.get(url)
            .then(response => {
                console.log("Dữ liệu trả về:", response.data);
                const wards = response.data.geonames || [];
                if (!wards || wards.length === 0) {
                    wardSelect.innerHTML = '<option value="">Không có dữ liệu xã/phường</option>';
                } else {
                    wards.forEach(ward => {
                        const option = document.createElement("option");
                        option.value = ward.name;
                        option.textContent = ward.name;
                        wardSelect.appendChild(option);
                    });
                }
            })
            .catch(error => {
                const errorMessage = error.response
                    ? (error.response.data.status ? error.response.data.status.message : error.response.data)
                    : error.message;
                console.error("Lỗi tải xã/phường:", errorMessage);
                wardSelect.innerHTML = `<option value="">Lỗi: ${errorMessage}</option>`;
            });
    }
});

// Hàm lấy geonameId của quốc gia
function getCountryGeonameId(countryCode) {
    const countryIds = {
        "AF": 1149361, // Afghanistan
        "AL": 783754, // Albania
        "DZ": 2589581, // Algeria
        "AD": 3041565, // Andorra
        "AO": 3351879, // Angola
        "AG": 3576396, // Antigua and Barbuda
        "AR": 3865483, // Argentina
        "AM": 174982, // Armenia
        "AU": 2077456, // Australia
        "AT": 2782113, // Austria
        "AZ": 587116, // Azerbaijan
        "BS": 3572887, // Bahamas
        "BH": 290291, // Bahrain
        "BD": 1210997, // Bangladesh
        "BB": 3374084, // Barbados
        "BY": 630336, // Belarus
        "BE": 2802361, // Belgium
        "BZ": 3582678, // Belize
        "BJ": 2395170, // Benin
        "BT": 1252634, // Bhutan
        "BO": 3923057, // Bolivia
        "BA": 3277605, // Bosnia and Herzegovina
        "BW": 933860, // Botswana
        "BR": 3469034, // Brazil
        "BN": 1820814, // Brunei
        "BG": 732800, // Bulgaria
        "BF": 2361809, // Burkina Faso
        "BI": 433561, // Burundi
        "KH": 1831722, // Cambodia
        "CM": 2233387, // Cameroon
        "CA": 6251999, // Canada
        "CV": 3374766, // Cape Verde
        "CF": 239880, // Central African Republic
        "TD": 2434508, // Chad
        "CL": 3895114, // Chile
        "CN": 1814991, // China
        "CO": 3686110, // Colombia
        "KM": 921929, // Comoros
        "CD": 203312, // Congo (Democratic Republic)
        "CG": 2260494, // Congo (Republic)
        "CR": 3624060, // Costa Rica
        "HR": 3202326, // Croatia
        "CU": 3562981, // Cuba
        "CY": 146669, // Cyprus
        "CZ": 3077311, // Czechia
        "DK": 2623032, // Denmark
        "DJ": 223816, // Djibouti
        "DO": 3508796, // Dominican Republic
        "EC": 3658394, // Ecuador
        "EG": 357994, // Egypt
        "SV": 3585968, // El Salvador
        "GQ": 2309096, // Equatorial Guinea
        "ER": 338010, // Eritrea
        "EE": 453733, // Estonia
        "ET": 337996, // Ethiopia
        "FJ": 2205218, // Fiji
        "FI": 660013, // Finland
        "FR": 3017382, // France
        "GA": 2400553, // Gabon
        "GM": 2413451, // Gambia
        "GE": 614540, // Georgia
        "DE": 2921044, // Germany
        "GH": 2300660, // Ghana
        "GR": 390903, // Greece
        "GT": 3595528, // Guatemala
        "GN": 2420477, // Guinea
        "GY": 3378535, // Guyana
        "HT": 3723988, // Haiti
        "HN": 3608932, // Honduras
        "HU": 719819, // Hungary
        "IS": 2629691, // Iceland
        "IN": 1269750, // India
        "ID": 1643084, // Indonesia
        "IR": 130758, // Iran
        "IQ": 99237, // Iraq
        "IE": 2963597, // Ireland
        "IT": 3175395, // Italy
        "JP": 1861060, // Japan
        "JO": 248816, // Jordan
        "KZ": 1522867, // Kazakhstan
        "KE": 192950, // Kenya
        "KR": 1835841, // South Korea
        "KW": 285570, // Kuwait
        "KG": 1527747, // Kyrgyzstan
        "LA": 1655842, // Laos
        "LV": 458258, // Latvia
        "LB": 272103, // Lebanon
        "LY": 2215636, // Libya
        "LT": 597427, // Lithuania
        "LU": 2960313, // Luxembourg
        "MG": 1062947, // Madagascar
        "MW": 927384, // Malawi
        "MY": 1733045, // Malaysia
        "MV": 1282028, // Maldives
        "ML": 2453866, // Mali
        "MT": 2562770, // Malta
        "MX": 3996063, // Mexico
        "MD": 617790, // Moldova
        "MC": 2993457, // Monaco
        "MN": 2029969, // Mongolia
        "MA": 2542007, // Morocco
        "MZ": 1036973, // Mozambique
        "MM": 1327865, // Myanmar
        "NA": 3355338, // Namibia
        "NP": 1282988, // Nepal
        "NL": 2750405, // Netherlands
        "NZ": 2186224, // New Zealand
        "NG": 2328926, // Nigeria
        "NO": 3144096, // Norway
        "OM": 286963, // Oman
        "PK": 1168579, // Pakistan
        "PA": 3703430, // Panama
        "PY": 3437598, // Paraguay
        "PE": 3932488, // Peru
        "PH": 1694008, // Philippines
        "PL": 798544, // Poland
        "PT": 2264397, // Portugal
        "QA": 289688, // Qatar
        "RO": 798549, // Romania
        "RU": 2017370, // Russia
        "SA": 102358, // Saudi Arabia
        "SG": 1880251, // Singapore
        "ZA": 953987, // South Africa
        "ES": 2510769, // Spain
        "SE": 2661886, // Sweden
        "CH": 2658434, // Switzerland
        "TH": 1605651, // Thailand
        "TR": 298795, // Turkey
        "UA": 690791, // Ukraine
        "AE": 290557, // United Arab Emirates
        "GB": 2635167, // United Kingdom
        "US": 6252001, // United States
        "VN": 1562822, // Vietnam
        "ZW": 878675,  // Zimbabwe
        "BQ": 7626844, // Bonaire, Sint Eustatius và Saba
        "SS": 7909807, // South Sudan (Nam Sudan)
        "TL": 1966436, // Timor-Leste (Đông Timor)
        "SX": 7609695, // Sint Maarten (Hà Lan)
        "XK": 831053,  // Kosovo
        // Các vùng lãnh thổ phụ thuộc
        "HK": 1819730, // Hong Kong (Trung Quốc)
        "MO": 1821275, // Macau (Trung Quốc)
        "FO": 2622320, // Quần đảo Faroe (Đan Mạch)
        "GG": 3042362, // Guernsey (Anh)
        "IM": 3042225, // Isle of Man (Anh)
        "JE": 3042142, // Jersey (Anh)
        "PF": 4030656, // Polynesia thuộc Pháp
        "NC": 2139685, // New Caledonia (Pháp)
        "PM": 3424932, // Saint Pierre và Miquelon (Pháp)
        "WF": 4034749,  // Wallis và Futuna (Pháp)
        "KI": 4030945, // Kiribati
        "MH": 2080185, // Marshall Islands (Quần đảo Marshall)
        "FM": 2081918, // Micronesia (Liên bang Micronesia)
        "NR": 2110425, // Nauru
        "PW": 1559582, // Palau
        "WS": 4034894, // Samoa
        "SM": 3168068, // San Marino
        "ST": 2410758, // Sao Tome and Principe
        "SC": 241170, // Seychelles
        "TV": 2110297, // Tuvalu
        "VA": 3164670,  // Vatican City (Holy See)
        "AI": 3573511, // Anguilla (Anh)
        "BM": 3573345, // Bermuda (Anh)
        "IO": 1282588, // British Indian Ocean Territory (Anh)
        "VG": 3577718, // British Virgin Islands (Anh)
        "KY": 3580718, // Cayman Islands (Anh)
        "FK": 3474414, // Falkland Islands (Anh)
        "GI": 2411586, // Gibraltar (Anh)
        "MS": 3578097, // Montserrat (Anh)
        "SH": 3370751, // Saint Helena, Ascension and Tristan da Cunha (Anh)
        "TC": 3576916, // Turks and Caicos Islands (Anh)
        "MP": 4041468, // Northern Mariana Islands (Mỹ)
        "GU": 4043988, // Guam (Mỹ)
        "AS": 5880801, // American Samoa (Mỹ)
        "VI": 4796775, // U.S. Virgin Islands (Mỹ)
        "RE": 935317, // Réunion (Pháp)
        "YT": 1024031, // Mayotte (Pháp)
        "GP": 3579143, // Guadeloupe (Pháp)
        "MF": 3578421, // Saint Martin (Pháp)
        "BL": 3578476, // Saint Barthélemy (Pháp)
        "CW": 7626836, // Curaçao (Hà Lan)
        "AW": 3577279, // Aruba (Hà Lan)
        "MF": 3578421  // Saint Martin (Hà Lan)
    };

    return countryIds[countryCode] || null;
}