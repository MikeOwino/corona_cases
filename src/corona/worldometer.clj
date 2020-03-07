(ns corona.worldometer
  (:require
   [corona.core :as c :refer [in?]]))

(def regions
  [
   ["China"                                        "Asia"     "Eastern Asia"              "1427647786" "1433783686" "+0.43%"]
   ["India"                                        "Asia"     "Southern Asia"             "1352642280" "1366417754" "+1.02%"]
   ["United States"                                "Americas" "Northern America"          "327096265"  "329064917"  "+0.60%"]
   ["Indonesia"                                    "Asia"     "South-eastern Asia"        "267670543"  "270625568"  "+1.10%"]
   ["Pakistan"                                     "Asia"     "Southern Asia"             "212228286"  "216565318"  "+2.04%"]
   ["Brazil"                                       "Americas" "South America"             "209469323"  "211049527"  "+0.75%"]
   ["Nigeria"                                      "Africa"   "Western Africa"            "195874683"  "200963599"  "+2.60%"]
   ["Bangladesh"                                   "Asia"     "Southern Asia"             "161376708"  "163046161"  "+1.03%"]
   ["Russia"                                       "Europe"   "Eastern Europe"            "145734038"  "145872256"  "+0.09%"]
   ["Mexico"                                       "Americas" "Central America"           "126190788"  "127575529"  "+1.10%"]
   ["Japan"                                        "Asia"     "Eastern Asia"              "127202192"  "126860301"  "−0.27%"]
   ["Ethiopia"                                     "Africa"   "Eastern Africa"            "109224414"  "112078730"  "+2.61%"]
   ["Philippines"                                  "Asia"     "South-eastern Asia"        "106651394"  "108116615"  "+1.37%"]
   ["Egypt"                                        "Africa"   "Northern Africa"           "98423598"   "100388073"  "+2.00%"]
   ["Vietnam"                                      "Asia"     "South-eastern Asia"        "95545962"   "96462106"   "+0.96%"]
   ["DR Congo"                                     "Africa"   "Middle Africa"             "84068091"   "86790567"   "+3.24%"]
   ["Germany"                                      "Europe"   "Western Europe"            "83124418"   "83517045"   "+0.47%"]
   ["Turkey"                                       "Asia"     "Western Asia"              "82340088"   "83429615"   "+1.32%"]
   ["Iran"                                         "Asia"     "Southern Asia"             "81800188"   "82913906"   "+1.36%"]
   ["Thailand"                                     "Asia"     "South-eastern Asia"        "68863514"   "69037513"   "+0.25%"]
   ["United Kingdom"                               "Europe"   "Northern Europe"           "67141684"   "67530172"   "+0.58%"]
   ["France"                                       "Europe"   "Western Europe"            "64990511"   "65129728"   "+0.21%"]
   ["Italy"                                        "Europe"   "Southern Europe"           "60627291"   "60550075"   "−0.13%"]
   ["South Africa"                                 "Africa"   "Southern Africa"           "57792518"   "58558270"   "+1.33%"]
   ["Tanzania"                                     "Africa"   "Eastern Africa"            "56313438"   "58005463"   "+3.00%"]
   ["Myanmar"                                      "Asia"     "South-eastern Asia"        "53708320"   "54045420"   "+0.63%"]
   ["Kenya"                                        "Africa"   "Eastern Africa"            "51392565"   "52573973"   "+2.30%"]
   ["South Korea"                                  "Asia"     "Eastern Asia"              "51171706"   "51225308"   "+0.10%"]
   ["Colombia"                                     "Americas" "South America"             "49661048"   "50339443"   "+1.37%"]
   ["Spain"                                        "Europe"   "Southern Europe"           "46692858"   "46736776"   "+0.09%"]
   ["Argentina"                                    "Americas" "South America"             "44361150"   "44780677"   "+0.95%"]
   ["Uganda"                                       "Africa"   "Eastern Africa"            "42729036"   "44269594"   "+3.61%"]
   ["Ukraine"                                      "Europe"   "Eastern Europe"            "44246156"   "43993638"   "−0.57%"]
   ["Algeria"                                      "Africa"   "Northern Africa"           "42228408"   "43053054"   "+1.95%"]
   ["Sudan"                                        "Africa"   "Northern Africa"           "41801533"   "42813238"   "+2.42%"]
   ["Iraq"                                         "Asia"     "Western Asia"              "38433600"   "39309783"   "+2.28%"]
   ["Afghanistan"                                  "Asia"     "Southern Asia"             "37171921"   "38041754"   "+2.34%"]
   ["Poland"                                       "Europe"   "Eastern Europe"            "37921592"   "37887768"   "−0.09%"]
   ["Canada"                                       "Americas" "Northern America"          "37074562"   "37411047"   "+0.91%"]
   ["Morocco"                                      "Africa"   "Northern Africa"           "36029093"   "36471769"   "+1.23%"]
   ["Saudi Arabia"                                 "Asia"     "Western Asia"              "33702756"   "34268528"   "+1.68%"]
   ["Uzbekistan"                                   "Asia"     "Central Asia"              "32476244"   "32981716"   "+1.56%"]
   ["Peru"                                         "Americas" "South America"             "31989260"   "32510453"   "+1.63%"]
   ["Malaysia"                                     "Asia"     "South-eastern Asia"        "31528033"   "31949777"   "+1.34%"]
   ["Angola"                                       "Africa"   "Middle Africa"             "30809787"   "31825295"   "+3.30%"]
   ["Mozambique"                                   "Africa"   "Eastern Africa"            "29496004"   "30366036"   "+2.95%"]
   ["Yemen"                                        "Asia"     "Western Asia"              "28498683"   "29161922"   "+2.33%"]
   ["Ghana"                                        "Africa"   "Western Africa"            "28206728"   "28833629"   "+2.22%"]
   ["Nepal"                                        "Asia"     "Southern Asia"             "28095714"   "28608710"   "+1.83%"]
   ["Venezuela"                                    "Americas" "South America"             "28887118"   "28515829"   "−1.29%"]
   ["Madagascar"                                   "Africa"   "Eastern Africa"            "26262313"   "26969307"   "+2.69%"]
   ["North Korea"                                  "Asia"     "Eastern Asia"              "25549604"   "25666161"   "+0.46%"]
   ["Ivory Coast"                                  "Africa"   "Western Africa"            "25069230"   "25716544"   "+2.58%"]
   ["Cameroon"                                     "Africa"   "Middle Africa"             "25216267"   "25876380"   "+2.62%"]
   ["Australia"                                    "Oceania"  "Australia and New Zealand" "24898152"   "25203198"   "+1.23%"]
   ["Taiwan"                                       "Asia"     "Eastern Asia"              "23726460"   "23773876"   "+0.20%"]
   ["Niger"                                        "Africa"   "Western Africa"            "22442822"   "23310715"   "+3.87%"]
   ["Sri Lanka"                                    "Asia"     "Southern Asia"             "21228763"   "21323733"   "+0.45%"]
   ["Burkina Faso"                                 "Africa"   "Western Africa"            "19751466"   "20321378"   "+2.89%"]
   ["Mali"                                         "Africa"   "Western Africa"            "19077749"   "19658031"   "+3.04%"]
   ["Romania"                                      "Europe"   "Eastern Europe"            "19506114"   "19364557"   "−0.73%"]
   ["Malawi"                                       "Africa"   "Eastern Africa"            "18143217"   "18628747"   "+2.68%"]
   ["Chile"                                        "Americas" "South America"             "18729160"   "18952038"   "+1.19%"]
   ["Kazakhstan"                                   "Asia"     "Central Asia"              "18319618"   "18551427"   "+1.27%"]
   ["Zambia"                                       "Africa"   "Eastern Africa"            "17351708"   "17861030"   "+2.94%"]
   ["Guatemala"                                    "Americas" "Central America"           "17247849"   "17581472"   "+1.93%"]
   ["Ecuador"                                      "Americas" "South America"             "17084358"   "17373662"   "+1.69%"]
   ["Netherlands"                                  "Europe"   "Western Europe"            "17059560"   "17097130"   "+0.22%"]
   ["Syria"                                        "Asia"     "Western Asia"              "16945057"   "17070135"   "+0.74%"]
   ["Cambodia"                                     "Asia"     "South-eastern Asia"        "16249792"   "16486542"   "+1.46%"]
   ["Senegal"                                      "Africa"   "Western Africa"            "15854323"   "16296364"   "+2.79%"]
   ["Chad"                                         "Africa"   "Middle Africa"             "15477729"   "15946876"   "+3.03%"]
   ["Somalia"                                      "Africa"   "Eastern Africa"            "15008226"   "15442905"   "+2.90%"]
   ["Zimbabwe"                                     "Africa"   "Eastern Africa"            "14438802"   "14645468"   "+1.43%"]
   ["Guinea"                                       "Africa"   "Western Africa"            "12414293"   "12771246"   "+2.88%"]
   ["Rwanda"                                       "Africa"   "Eastern Africa"            "12301970"   "12626950"   "+2.64%"]
   ["Benin"                                        "Africa"   "Western Africa"            "11485044"   "11801151"   "+2.75%"]
   ["Tunisia"                                      "Africa"   "Northern Africa"           "11565201"   "11694719"   "+1.12%"]
   ["Belgium"                                      "Europe"   "Western Europe"            "11482178"   "11539328"   "+0.50%"]
   ["Bolivia"                                      "Americas" "South America"             "11353142"   "11513100"   "+1.41%"]
   ["Cuba"                                         "Americas" "Caribbean"                 "11338134"   "11333483"   "−0.04%"]
   ["Haiti"                                        "Americas" "Caribbean"                 "11123178"   "11263770"   "+1.26%"]
   ["South Sudan"                                  "Africa"   "Eastern Africa"            "10975927"   "11062113"   "+0.79%"]
   ["Burundi"                                      "Africa"   "Eastern Africa"            "10524117"   "10864245"   "+3.23%"]
   ["Dominican Republic"                           "Americas" "Caribbean"                 "10627141"   "10738958"   "+1.05%"]
   ["Czech Republic"                               "Europe"   "Eastern Europe"            "10665677"   "10689209"   "+0.22%"]
   ["Greece"                                       "Europe"   "Southern Europe"           "10522246"   "10473455"   "−0.46%"]
   ["Portugal"                                     "Europe"   "Southern Europe"           "10256193"   "10226187"   "−0.29%"]
   ["Jordan"                                       "Asia"     "Western Asia"              "9965318"    "10101694"   "+1.37%"]
   ["Azerbaijan"                                   "Asia"     "Western Asia"              "9949537"    "10047718"   "+0.99%"]
   ["Sweden"                                       "Europe"   "Northern Europe"           "9971638"    "10036379"   "+0.65%"]
   ["United Arab Emirates"                         "Asia"     "Western Asia"              "9630959"    "9770529"    "+1.45%"]
   ["Honduras"                                     "Americas" "Central America"           "9587522"    "9746117"    "+1.65%"]
   ["Hungary"                                      "Europe"   "Eastern Europe"            "9707499"    "9684679"    "−0.24%"]
   ["Belarus"                                      "Europe"   "Eastern Europe"            "9452617"    "9452411"    "0.00%"]
   ["Tajikistan"                                   "Asia"     "Central Asia"              "9100835"    "9321018"    "+2.42%"]
   ["Austria"                                      "Europe"   "Western Europe"            "8891388"    "8955102"    "+0.72%"]
   ["Papua New Guinea"                             "Oceania"  "Melanesia"                 "8606323"    "8776109"    "+1.97%"]
   ["Serbia"                                       "Europe"   "Southern Europe"           "8802754"    "8772235"    "−0.35%"]
   [" Switzerland"                                 "Europe"   "Western Europe"            "8525611"    "8591365"    "+0.77%"]
   ["Israel"                                       "Asia"     "Western Asia"              "8381516"    "8519377"    "+1.64%"]
   ["Togo"                                         "Africa"   "Western Africa"            "7889093"    "8082366"    "+2.45%"]
   ["Sierra Leone"                                 "Africa"   "Western Africa"            "7650150"    "7813215"    "+2.13%"]
   ["Hong Kong"                                    "Asia"     "Eastern Asia"              "7371730"    "7436154"    "+0.87%"]
   ["Laos"                                         "Asia"     "South-eastern Asia"        "7061507"    "7169455"    "+1.53%"]
   ["Paraguay"                                     "Americas" "South America"             "6956066"    "7044636"    "+1.27%"]
   ["Bulgaria"                                     "Europe"   "Eastern Europe"            "7051608"    "7000119"    "−0.73%"]
   ["Lebanon"                                      "Asia"     "Western Asia"              "6859408"    "6855713"    "−0.05%"]
   ["Libya"                                        "Africa"   "Northern Africa"           "6678559"    "6777452"    "+1.48%"]
   ["Nicaragua"                                    "Americas" "Central America"           "6465501"    "6545502"    "+1.24%"]
   ["El Salvador"                                  "Americas" "Central America"           "6420746"    "6453553"    "+0.51%"]
   ["Kyrgyzstan"                                   "Asia"     "Central Asia"              "6304030"    "6415850"    "+1.77%"]
   ["Turkmenistan"                                 "Asia"     "Central Asia"              "5850901"    "5942089"    "+1.56%"]
   ["Singapore"                                    "Asia"     "South-eastern Asia"        "5757499"    "5804337"    "+0.81%"]
   ["Denmark"                                      "Europe"   "Northern Europe"           "5752126"    "5771876"    "+0.34%"]
   ["Finland"                                      "Europe"   "Northern Europe"           "5522576"    "5532156"    "+0.17%"]
   ["Slovakia"                                     "Europe"   "Eastern Europe"            "5453014"    "5457013"    "+0.07%"]
   ["Congo"                                        "Africa"   "Middle Africa"             "5244359"    "5380508"    "+2.60%"]
   ["Norway"                                       "Europe"   "Northern Europe"           "5337962"    "5378857"    "+0.77%"]
   ["Costa Rica"                                   "Americas" "Central America"           "4999441"    "5047561"    "+0.96%"]
   ["Palestine"                                    "Asia"     "Western Asia"              "4862979"    "4981420"    "+2.44%"]
   ["Oman"                                         "Asia"     "Western Asia"              "4829473"    "4974986"    "+3.01%"]
   ["Liberia"                                      "Africa"   "Western Africa"            "4818973"    "4937374"    "+2.46%"]
   ["Ireland"                                      "Europe"   "Northern Europe"           "4818690"    "4882495"    "+1.32%"]
   ["New Zealand"                                  "Oceania"  "Australia and New Zealand" "4743131"    "4783063"    "+0.84%"]
   ["Central African Republic"                     "Africa"   "Middle Africa"             "4666368"    "4745185"    "+1.69%"]
   ["Mauritania"                                   "Africa"   "Western Africa"            "4403313"    "4525696"    "+2.78%"]
   ["Panama"                                       "Americas" "Central America"           "4176869"    "4246439"    "+1.67%"]
   ["Kuwait"                                       "Asia"     "Western Asia"              "4137312"    "4207083"    "+1.69%"]
   ["Croatia"                                      "Europe"   "Southern Europe"           "4156405"    "4130304"    "−0.63%"]
   ["Moldova"                                      "Europe"   "Eastern Europe"            "4051944"    "4043263"    "−0.21%"]
   ["Georgia"                                      "Asia"     "Western Asia"              "4002942"    "3996765"    "−0.15%"]
   ["Eritrea"                                      "Africa"   "Eastern Africa"            "3452786"    "3497117"    "+1.28%"]
   ["Uruguay"                                      "Americas" "South America"             "3449285"    "3461734"    "+0.36%"]
   ["Bosnia and Herzegovina"                       "Europe"   "Southern Europe"           "3323925"    "3301000"    "−0.69%"]
   ["Mongolia"                                     "Asia"     "Eastern Asia"              "3170216"    "3225167"    "+1.73%"]
   ["Armenia"                                      "Asia"     "Western Asia"              "2951745"    "2957731"    "+0.20%"]
   ["Jamaica"                                      "Americas" "Caribbean"                 "2934847"    "2948279"    "+0.46%"]
   ["Puerto Rico"                                  "Americas" "Caribbean"                 "3039596"    "2933408"    "−3.49%"]
   ["Albania"                                      "Europe"   "Southern Europe"           "2882740"    "2880917"    "−0.06%"]
   ["Qatar"                                        "Asia"     "Western Asia"              "2781682"    "2832067"    "+1.81%"]
   ["Lithuania"                                    "Europe"   "Northern Europe"           "2801264"    "2759627"    "−1.49%"]
   ["Namibia"                                      "Africa"   "Southern Africa"           "2448301"    "2494530"    "+1.89%"]
   ["Gambia"                                       "Africa"   "Western Africa"            "2280094"    "2347706"    "+2.97%"]
   ["Botswana"                                     "Africa"   "Southern Africa"           "2254068"    "2303697"    "+2.20%"]
   ["Gabon"                                        "Africa"   "Middle Africa"             "2119275"    "2172579"    "+2.52%"]
   ["Lesotho"                                      "Africa"   "Southern Africa"           "2108328"    "2125268"    "+0.80%"]
   ["North Macedonia"                              "Europe"   "Southern Europe"           "2082957"    "2083459"    "+0.02%"]
   ["Slovenia"                                     "Europe"   "Southern Europe"           "2077837"    "2078654"    "+0.04%"]
   ["Guinea-Bissau"                                "Africa"   "Western Africa"            "1874303"    "1920922"    "+2.49%"]
   ["Latvia"                                       "Europe"   "Northern Europe"           "1928459"    "1906743"    "−1.13%"]
   ["Bahrain"                                      "Asia"     "Western Asia"              "1569446"    "1641172"    "+4.57%"]
   ["Trinidad and Tobago"                          "Americas" "Caribbean"                 "1389843"    "1394973"    "+0.37%"]
   ["Equatorial Guinea"                            "Africa"   "Middle Africa"             "1308975"    "1355986"    "+3.59%"]
   ["Estonia"                                      "Europe"   "Northern Europe"           "1322920"    "1325648"    "+0.21%"]
   ["East Timor"                                   "Asia"     "South-eastern Asia"        "1267974"    "1293119"    "+1.98%"]
   ["Mauritius"                                    "Africa"   "Eastern Africa"            "1189265"    "1198575"    "+0.78%"]
   ["Cyprus"                                       "Asia"     "Western Asia"              "1170125"    "1179551"    "+0.81%"]
   ["Eswatini"                                     "Africa"   "Southern Africa"           "1136281"    "1148130"    "+1.04%"]
   ["Djibouti"                                     "Africa"   "Eastern Africa"            "958923"     "973560"     "+1.53%"]
   ["Fiji"                                         "Oceania"  "Melanesia"                 "883483"     "889953"     "+0.73%"]
   ["Réunion"                                      "Africa"   "Eastern Africa"            "882526"     "888927"     "+0.73%"]
   ["Comoros"                                      "Africa"   "Eastern Africa"            "832322"     "850886"     "+2.23%"]
   ["Guyana"                                       "Americas" "South America"             "779006"     "782766"     "+0.48%"]
   ["Bhutan"                                       "Asia"     "Southern Asia"             "754388"     "763092"     "+1.15%"]
   ["Solomon Islands"                              "Oceania"  "Melanesia"                 "652857"     "669823"     "+2.60%"]
   ["Macau"                                        "Asia"     "Eastern Asia"              "631636"     "640445"     "+1.39%"]
   ["Montenegro"                                   "Europe"   "Southern Europe"           "627809"     "627987"     "+0.03%"]
   ["Luxembourg"                                   "Europe"   "Western Europe"            "604245"     "615729"     "+1.90%"]
   ["Western Sahara"                               "Africa"   "Northern Africa"           "567402"     "582463"     "+2.65%"]
   ["Suriname"                                     "Americas" "South America"             "575990"     "581372"     "+0.93%"]
   ["Cape Verde"                                   "Africa"   "Western Africa"            "543767"     "549935"     "+1.13%"]
   ["Maldives"                                     "Asia"     "Southern Asia"             "515696"     "530953"     "+2.96%"]
   ["Guadeloupe"                                   "Americas" "Caribbean"                 "446928"     "447905"     "+0.22%"]
   ["Malta"                                        "Europe"   "Southern Europe"           "439248"     "440372"     "+0.26%"]
   ["Brunei"                                       "Asia"     "South-eastern Asia"        "428963"     "433285"     "+1.01%"]
   ["Belize"                                       "Americas" "Central America"           "383071"     "390353"     "+1.90%"]
   ["Bahamas"                                      "Americas" "Caribbean"                 "385637"     "389482"     "+1.00%"]
   ["Martinique"                                   "Americas" "Caribbean"                 "375673"     "375554"     "−0.03%"]
   ["Iceland"                                      "Europe"   "Northern Europe"           "336713"     "339031"     "+0.69%"]
   ["Vanuatu"                                      "Oceania"  "Melanesia"                 "292680"     "299882"     "+2.46%"]
   ["Barbados"                                     "Americas" "Caribbean"                 "286641"     "287025"     "+0.13%"]
   ["New Caledonia"                                "Oceania"  "Melanesia"                 "279993"     "282750"     "+0.98%"]
   ["French Guiana"                                "Americas" "South America"             "275713"     "282731"     "+2.55%"]
   ["French Polynesia"                             "Oceania"  "Polynesia"                 "277679"     "279287"     "+0.58%"]
   ["Mayotte"                                      "Africa"   "Eastern Africa"            "259531"     "266150"     "+2.55%"]
   ["São Tomé and Príncipe"                        "Africa"   "Middle Africa"             "211028"     "215056"     "+1.91%"]
   ["Samoa"                                        "Oceania"  "Polynesia"                 "196129"     "197097"     "+0.49%"]
   ["Saint Lucia"                                  "Americas" "Caribbean"                 "181889"     "182790"     "+0.50%"]
   ["Guernsey and  Jersey"                         "Europe"   "Northern Europe"           "170499"     "172259"     "+1.03%"]
   ["Guam"                                         "Oceania"  "Micronesia"                "165768"     "167294"     "+0.92%"]
   ["Curaçao"                                      "Americas" "Caribbean"                 "162752"     "163424"     "+0.41%"]
   ["Kiribati"                                     "Oceania"  "Micronesia"                "115847"     "117606"     "+1.52%"]
   ["F.S. Micronesia"                              "Oceania"  "Micronesia"                "112640"     "113815"     "+1.04%"]
   ["Grenada"                                      "Americas" "Caribbean"                 "111454"     "112003"     "+0.49%"]
   ["Tonga"                                        "Oceania"  "Polynesia"                 "110589"     "110940"     "+0.32%"]
   ["Saint Vincent and the Grenadines"             "Americas" "Caribbean"                 "110211"     "110589"     "+0.34%"]
   ["Aruba"                                        "Americas" "Caribbean"                 "105845"     "106314"     "+0.44%"]
   ["U.S. Virgin Islands"                          "Americas" "Caribbean"                 "104680"     "104578"     "−0.10%"]
   ["Seychelles"                                   "Africa"   "Eastern Africa"            "97096"      "97739"      "+0.66%"]
   ["Antigua and Barbuda"                          "Americas" "Caribbean"                 "96286"      "97118"      "+0.86%"]
   ["Isle of Man"                                  "Europe"   "Northern Europe"           "84077"      "84584"      "+0.60%"]
   ["Andorra"                                      "Europe"   "Southern Europe"           "77006"      "77142"      "+0.18%"]
   ["Dominica"                                     "Americas" "Caribbean"                 "71625"      "71808"      "+0.26%"]
   ["Cayman Islands"                               "Americas" "Caribbean"                 "64174"      "64948"      "+1.21%"]
   ["Bermuda"                                      "Americas" "Northern America"          "62756"      "62506"      "−0.40%"]
   ["Marshall Islands"                             "Oceania"  "Micronesia"                "58413"      "58791"      "+0.65%"]
   ["Greenland"                                    "Americas" "Northern America"          "56564"      "56672"      "+0.19%"]
   ["Northern Mariana Islands"                     "Oceania"  "Micronesia"                "56882"      "56188"      "−1.22%"]
   ["American Samoa"                               "Oceania"  "Polynesia"                 "55465"      "55312"      "−0.28%"]
   ["Saint Kitts and Nevis"                        "Americas" "Caribbean"                 "52441"      "52823"      "+0.73%"]
   ["Faroe Islands"                                "Europe"   "Northern Europe"           "48497"      "48678"      "+0.37%"]
   ["Sint Maarten"                                 "Americas" "Caribbean"                 "41940"      "42388"      "+1.07%"]
   ["Monaco"                                       "Europe"   "Western Europe"            "38682"      "38964"      "+0.73%"]
   ["Turks and Caicos Islands"                     "Americas" "Caribbean"                 "37665"      "38191"      "+1.40%"]
   ["Liechtenstein"                                "Europe"   "Western Europe"            "37910"      "38019"      "+0.29%"]
   ["San Marino"                                   "Europe"   "Southern Europe"           "33785"      "33860"      "+0.22%"]
   ["Gibraltar"                                    "Europe"   "Southern Europe"           "33718"      "33701"      "−0.05%"]
   ["British Virgin Islands"                       "Americas" "Caribbean"                 "29802"      "30030"      "+0.77%"]
   ["Caribbean Netherlands"                        "Americas" "Caribbean"                 "25711"      "25979"      "+1.04%"]
   ["Palau"                                        "Oceania"  "Micronesia"                "17907"      "18008"      "+0.56%"]
   ["Cook Islands"                                 "Oceania"  "Polynesia"                 "17518"      "17548"      "+0.17%"]
   ["Anguilla"                                     "Americas" "Caribbean"                 "14731"      "14869"      "+0.94%"]
   ["Tuvalu"                                       "Oceania"  "Polynesia"                 "11508"      "11646"      "+1.20%"]
   ["Wallis and Futuna"                            "Oceania"  "Polynesia"                 "11661"      "11432"      "−1.96%"]
   ["Nauru"                                        "Oceania"  "Micronesia"                "10670"      "10756"      "+0.81%"]
   ["Saint Helena, Ascension and Tristan da Cunha" "Africa"   "Western Africa"            "6035"       "6059"       "+0.40%"]
   ["Saint Pierre and Miquelon"                    "Americas" "Northern America"          "5849"       "5822"       "−0.46%"]
   ["Montserrat"                                   "Americas" "Caribbean"                 "4993"       "4989"       "−0.08%"]
   ["Falkland Islands"                             "Americas" "South America"             "3234"       "3377"       "+4.42%"]
   ["Niue"                                         "Oceania"  "Polynesia"                 "1620"       "1615"       "−0.31%"]
   ["Tokelau"                                      "Oceania"  "Polynesia"                 "1319"       "1340"       "+1.59%"]
   ["Vatican City"                                 "Europe"   "Southern Europe"           "801"        "799"        "−0.25%"]
   ]
  )

(def data
  "TODO create spec. E.g. Rate is percentage must be between 0 and 100

  Country
  Population
  YearlyChangeRate
  NetChange
  Density
  LandArea
  Migrants
  FertilityRate
  MedianAge
  UrbanPopulationRate
  WorldShareRate
  "
  [
   ["China"                    1439323776 0.39 5540090 153 9388211 -348399 1.7 38 61 18.47]
   ["India"                    1380004385 0.99 13586631 464 2973190 -532687 2.2 28 35 17.7]
   ["United States"            331002651 0.59 1937734 36 9147420 954806 1.8 38 83 4.25]
   ["Indonesia"                273523615 1.07 2898047 151 1811570 -98955 2.3 30 56 3.51]
   ["Pakistan"                 220892340 2.0 4327022 287 770880 -233379 3.6 23 35 2.83]
   ["Brazil"                   212559417 0.72 1509890 25 8358140 21200 1.7 33 88 2.73]
   ["Nigeria"                  206139589 2.58 5175990 226 910770 -60000 5.4 18 52 2.64]
   ["Bangladesh"               164689383 1.01 1643222 1265 130170 -369501 2.1 28 39 2.11]
   ["Russia"                   145934462 0.04 62206 9 16376870 182456 1.8 40 74 1.87]
   ["Mexico"                   128932753 1.06 1357224 66 1943950 -60000 2.1 29 84 1.65]
   ["Japan"                    126476461 -0.3 -383840 347 364555 71560 1.4 48 92 1.62]
   ["Ethiopia"                 114963588 2.57 2884858 115 1000000 30000 4.3 19 21 1.47]
   ["Philippines"              109581078 1.35 1464463 368 298170 -67152 2.6 26 47 1.41]
   ["Egypt"                    102334404 1.94 1946331 103 995450 -38033 3.3 25 43 1.31]
   ["Vietnam"                  97338579 0.91 876473 314 310070 -80000 2.1 32 38 1.25]
   ["DR Congo"                 89561403 3.19 2770836 40 2267050 23861 6 17 46 1.15]
   ["Turkey"                   84339067 1.09 909452 110 769630 283922 2.1 32 76 1.08]
   ["Iran"                     83992949 1.3 1079043 52 1628550 -55000 2.2 32 76 1.08]
   ["Germany"                  83783942 0.32 266897 240 348560 543822 1.6 46 76 1.07]
   ["Thailand"                 69799978 0.25 174396 137 510890 19444 1.5 40 51 0.9]
   ["United Kingdom"           67886011 0.53 355839 281 241930 260650 1.8 40 83 0.87]
   ["France"                   65273511 0.22 143783 119 547557 36527 1.9 42 82 0.84]
   ["Italy"                    60461826 -0.15 -88249 206 294140 148943 1.3 47 69 0.78]
   ["Tanzania"                 59734218 2.98 1728755 67 885800 -40076 4.9 18 37 0.77]
   ["South Africa"             59308690 1.28 750420 49 1213090 145405 2.4 28 67 0.76]
   ["Myanmar"                  54409800 0.67 364380 83 653290 -163313 2.2 29 31 0.7]
   ["Kenya"                    53771296 2.28 1197323 94 569140 -10000 3.5 20 28 0.69]
   ["South Korea"              51269185 0.09 43877 527 97230 11731 1.1 44 82 0.66]
   ["Colombia"                 50882891 1.08 543448 46 1109500 204796 1.8 31 80 0.65]
   ["Spain"                    46754778 0.04 18002 94 498800 40000 1.3 45 80 0.6]
   ["Uganda"                   45741007 3.32 1471413 229 199810 168694 5 17 26 0.59]
   ["Argentina"                45195774 0.93 415097 17 2736690 4800 2.3 32 93 0.58]
   ["Algeria"                  43851044 1.85 797990 18 2381740 -10000 3.1 29 73 0.56]
   ["Sudan"                    43849260 2.42 1036022 25 1765048 -50000 4.4 20 35 0.56]
   ["Ukraine"                  43733762 -0.59 -259876 75 579320 10000 1.4 41 69 0.56]
   ["Iraq"                     40222493 2.32 912710 93 434320 7834 3.7 21 73 0.52]
   ["Afghanistan"              38928346 2.33 886592 60 652860 -62920 4.6 18 25 0.5]
   ["Poland"                   37846611 -0.11 -41157 124 306230 -29395 1.4 42 60 0.49]
   ["Canada"                   37742154 0.89 331107 4 9093510 242032 1.5 41 81 0.48]
   ["Morocco"                  36910560 1.2 438791 83 446300 -51419 2.4 30 64 0.47]
   ["Saudi Arabia"             34813871 1.59 545343 16 2149690 134979 2.3 32 84 0.45]
   ["Uzbekistan"               33469203 1.48 487487 79 425400 -8863 2.4 28 50 0.43]
   ["Peru"                     32971854 1.42 461401 26 1280000 99069 2.3 31 79 0.42]
   ["Angola"                   32866272 3.27 1040977 26 1246700 6413 5.6 17 67 0.42]
   ["Malaysia"                 32365999 1.3 416222 99 328550 50000 2 30 78 0.42]
   ["Mozambique"               31255435 2.93 889399 40 786380 -5000 4.9 18 38 0.4]
   ["Ghana"                    31072940 2.15 655084 137 227540 -10000 3.9 22 57 0.4]
   ["Yemen"                    29825964 2.28 664042 56 527970 -30000 3.8 20 38 0.38]
   ["Nepal"                    29136808 1.85 528098 203 143350 41710 1.9 25 21 0.37]
   ["Venezuela"                28435940 -0.28 -79889 32 882050 -653249 2.3 30 nil 0.36]
   ["Madagascar"               27691018 2.68 721711 48 581795 -1500 4.1 20 39 0.36]
   ["Cameroon"                 26545863 2.59 669483 56 472710 -4800 4.6 19 56 0.34]
   ["Côte d'Ivoire"            26378274 2.57 661730 83 318000 -8000 4.7 19 51 0.34]
   ["North Korea"              25778816 0.44 112655 214 120410 -5403 1.9 35 63 0.33]
   ["Australia"                25499884 1.18 296686 3 7682300 158246 1.8 38 86 0.33]
   ["Niger"                    24206644 3.84 895929 19 1266700 4000 7 15 17 0.31]
   ["Taiwan"                   23816775 0.18 42899 673 35410 30001 1.2 42 79 0.31]
   ["Sri Lanka"                21413249 0.42 89516 341 62710 -97986 2.2 34 18 0.27]
   ["Burkina Faso"             20903273 2.86 581895 76 273600 -25000 5.2 18 31 0.27]
   ["Mali"                     20250833 3.02 592802 17 1220190 -40000 5.9 16 44 0.26]
   ["Romania"                  19237691 -0.66 -126866 84 230170 -73999 1.6 43 55 0.25]
   ["Malawi"                   19129952 2.69 501205 203 94280 -16053 4.3 18 18 0.25]
   ["Chile"                    19116201 0.87 164163 26 743532 111708 1.7 35 85 0.25]
   ["Kazakhstan"               18776707 1.21 225280 7 2699700 -18000 2.8 31 58 0.24]
   ["Zambia"                   18383955 2.93 522925 25 743390 -8000 4.7 18 45 0.24]
   ["Guatemala"                17915568 1.9 334096 167 107160 -9215 2.9 23 52 0.23]
   ["Ecuador"                  17643054 1.55 269392 71 248360 36400 2.4 28 63 0.23]
   ["Syria"                    17500658 2.52 430523 95 183630 -427391 2.8 26 60 0.22]
   ["Netherlands"              17134872 0.22 37742 508 33720 16000 1.7 43 92 0.22]
   ["Senegal"                  16743927 2.75 447563 87 192530 -20000 4.7 19 49 0.21]
   ["Cambodia"                 16718965 1.41 232423 95 176520 -30000 2.5 26 24 0.21]
   ["Chad"                     16425864 3.0 478988 13 1259200 2000 5.8 17 23 0.21]
   ["Somalia"                  15893222 2.92 450317 25 627340 -40000 6.1 17 47 0.2]
   ["Zimbabwe"                 14862924 1.48 217456 38 386850 -116858 3.6 19 38 0.19]
   ["Guinea"                   13132795 2.83 361549 53 245720 -4000 4.7 18 39 0.17]
   ["Rwanda"                   12952218 2.58 325268 525 24670 -9000 4.1 20 18 0.17]
   ["Benin"                    12123200 2.73 322049 108 112760 -2000 4.9 19 48 0.16]
   ["Burundi"                  11890784 3.12 360204 463 25680 2001 5.5 17 14 0.15]
   ["Tunisia"                  11818619 1.06 123900 76 155360 -4000 2.2 33 70 0.15]
   ["Bolivia"                  11673021 1.39 159921 11 1083300 -9504 2.8 26 69 0.15]
   ["Belgium"                  11589623 0.44 50295 383 30280 48000 1.7 42 98 0.15]
   ["Haiti"                    11402528 1.24 139451 414 27560 -35000 3 24 57 0.15]
   ["Cuba"                     11326616 -0.06 -6867 106 106440 -14400 1.6 42 78 0.15]
   ["South Sudan"              11193725 1.19 131612 18 610952 -174200 4.7 19 25 0.14]
   ["Dominican Republic"       10847910 1.01 108952 225 48320 -30000 2.4 28 85 0.14]
   ["Czech Republic"           10708981 0.18 19772 139 77240 22011 1.6 43 74 0.14]
   ["Greece"                   10423054 -0.48 -50401 81 128900 -16000 1.3 46 85 0.13]
   ["Jordan"                   10203134 1.0 101440 115 88780 10220 2.8 24 91 0.13]
   ["Portugal"                 10196709 -0.29 -29478 111 91590 -6000 1.3 46 66 0.13]
   ["Azerbaijan"               10139177 0.91 91459 123 82658 1200 2.1 32 56 0.13]
   ["Sweden"                   10099265 0.63 62886 25 410340 40000 1.9 41 88 0.13]
   ["Honduras"                 9904607 1.63 158490 89 111890 -6800 2.5 24 57 0.13]
   ["United Arab Emirates"     9890402 1.23 119873 118 83600 40000 1.4 33 86 0.13]
   ["Hungary"                  9660351 -0.25 -24328 107 90530 6000 1.5 43 72 0.12]
   ["Tajikistan"               9537645 2.32 216627 68 139960 -20000 3.6 22 27 0.12]
   ["Belarus"                  9449323 -0.03 -3088 47 202910 8730 1.7 40 79 0.12]
   ["Austria"                  9006398 0.57 51296 109 82409 65000 1.5 43 57 0.12]
   ["Papua New Guinea"         8947024 1.95 170915 20 452860 -800 3.6 22 13 0.11]
   ["Serbia"                   8737371 -0.4 -34864 100 87460 4000 1.5 42 56 0.11]
   ["Israel"                   8655535 1.6 136158 400 21640 10000 3 30 93 0.11]
   ["Switzerland"              8654622 0.74 63257 219 39516 52000 1.5 43 74 0.11]
   ["Togo"                     8278724 2.43 196358 152 54390 -2000 4.4 19 43 0.11]
   ["Sierra Leone"             7976983 2.1 163768 111 72180 -4200 4.3 19 43 0.1]
   ["Hong Kong"                7496981 0.82 60827 7140 1050 29308 1.3 45 nil 0.1]
   ["Laos"                     7275560 1.48 106105 32 230800 -14704 2.7 24 36 0.09]
   ["Paraguay"                 7132538 1.25 87902 18 397300 -16556 2.4 26 62 0.09]
   ["Bulgaria"                 6948445 -0.74 -51674 64 108560 -4800 1.6 45 76 0.09]
   ["Libya"                    6871292 1.38 93840 4 1759540 -1999 2.3 29 78 0.09]
   ["Lebanon"                  6825445 -0.44 -30268 667 10230 -30012 2.1 30 78 0.09]
   ["Nicaragua"                6624554 1.21 79052 55 120340 -21272 2.4 26 57 0.08]
   ["Kyrgyzstan"               6524195 1.69 108345 34 191800 -4000 3 26 36 0.08]
   ["El Salvador"              6486205 0.51 32652 313 20720 -40539 2.1 28 73 0.08]
   ["Turkmenistan"             6031200 1.5 89111 13 469930 -5000 2.8 27 53 0.08]
   ["Singapore"                5850342 0.79 46005 8358 700 27028 1.2 42 nil 0.08]
   ["Denmark"                  5792202 0.35 20326 137 42430 15200 1.8 42 88 0.07]
   ["Finland"                  5540720 0.15 8564 18 303890 14000 1.5 43 86 0.07]
   ["Congo"                    5518087 2.56 137579 16 341500 -4000 4.5 19 70 0.07]
   ["Slovakia"                 5459642 0.05 2629 114 48088 1485 1.5 41 54 0.07]
   ["Norway"                   5421241 0.79 42384 15 365268 28000 1.7 40 83 0.07]
   ["Oman"                     5106626 2.65 131640 16 309500 87400 2.9 31 87 0.07]
   ["State of Palestine"       5101414 2.41 119994 847 6020 -10563 3.7 21 80 0.07]
   ["Costa Rica"               5094118 0.92 46557 100 51060 4200 1.8 33 80 0.07]
   ["Liberia"                  5057681 2.44 120307 53 96320 -5000 4.4 19 53 0.06]
   ["Ireland"                  4937786 1.13 55291 72 68890 23604 1.8 38 63 0.06]
   ["Central African Republic" 4829767 1.78 84582 8 622980 -40000 4.8 18 43 0.06]
   ["New Zealand"              4822233 0.82 39170 18 263310 14881 1.9 38 87 0.06]
   ["Mauritania"               4649658 2.74 123962 5 1030700 5000 4.6 20 57 0.06]
   ["Panama"                   4314767 1.61 68328 58 74340 11200 2.5 30 68 0.06]
   ["Kuwait"                   4270571 1.51 63488 240 17820 39520 2.1 37 nil 0.05]
   ["Croatia"                  4105267 -0.61 -25037 73 55960 -8001 1.4 44 58 0.05]
   ["Moldova"                  4033963 -0.23 -9300 123 32850 -1387 1.3 38 43 0.05]
   ["Georgia"                  3989167 -0.19 -7598 57 69490 -10000 2.1 38 58 0.05]
   ["Eritrea"                  3546421 1.41 49304 35 101000 -39858 4.1 19 63 0.05]
   ["Uruguay"                  3473730 0.35 11996 20 175020 -3000 2 36 96 0.04]
   ["Bosnia and Herzegovina"   3280819 -0.61 -20181 64 51000 -21585 1.3 43 52 0.04]
   ["Mongolia"                 3278290 1.65 53123 2 1553560 -852 2.9 28 67 0.04]
   ["Armenia"                  2963243 0.19 5512 104 28470 -4998 1.8 35 63 0.04]
   ["Jamaica"                  2961167 0.44 12888 273 10830 -11332 2 31 55 0.04]
   ["Qatar"                    2881053 1.73 48986 248 11610 40000 1.9 32 96 0.04]
   ["Albania"                  2877797 -0.11 -3120 105 27400 -14000 1.6 36 63 0.04]
   ["Puerto Rico"              2860853 -2.47 -72555 323 8870 -97986 1.2 44 nil 0.04]
   ["Lithuania"                2722289 -1.35 -37338 43 62674 -32780 1.7 45 71 0.03]
   ["Namibia"                  2540905 1.86 46375 3 823290 -4806 3.4 22 55 0.03]
   ["Gambia"                   2416668 2.94 68962 239 10120 -3087 5.3 18 59 0.03]
   ["Botswana"                 2351627 2.08 47930 4 566730 3000 2.9 24 73 0.03]
   ["Gabon"                    2225734 2.45 53155 9 257670 3260 4 23 87 0.03]
   ["Lesotho"                  2142249 0.8 16981 71 30360 -10047 3.2 24 31 0.03]
   ["North Macedonia"          2083374 0.0 -85 83 25220 -1000 1.5 39 59 0.03]
   ["Slovenia"                 2078938 0.01 284 103 20140 2000 1.6 45 55 0.03]
   ["Guinea-Bissau"            1968001 2.45 47079 70 28120 -1399 4.5 19 45 0.03]
   ["Latvia"                   1886198 -1.08 -20545 30 62200 -14837 1.7 44 69 0.02]
   ["Bahrain"                  1701575 3.68 60403 2239 760 47800 2 32 89 0.02]
   ["Equatorial Guinea"        1402985 3.47 46999 50 28050 16000 4.6 22 73 0.02]
   ["Trinidad and Tobago"      1399488 0.32 4515 273 5130 -800 1.7 36 52 0.02]
   ["Estonia"                  1326535 0.07 887 31 42390 3911 1.6 42 68 0.02]
   ["Timor-Leste"              1318445 1.96 25326 89 14870 -5385 4.1 21 33 0.02]
   ["Mauritius"                1271768 0.17 2100 626 2030 0 1.4 37 41 0.02]
   ["Cyprus"                   1207359 0.73 8784 131 9240 5000 1.3 37 67 0.02]
   ["Eswatini"                 1160164 1.05 12034 67 17200 -8353 3 21 30 0.01]
   ["Djibouti"                 988000 1.48 14440 43 23180 900 2.8 27 79 0.01]
   ["Fiji"                     896445 0.73 6492 49 18270 -6202 2.8 28 59 0.01]
   ["Réunion"                  895312 0.72 6385 358 2500 -1256 2.3 36 100 0.01]
   ["Comoros"                  869601 2.2 18715 467 1861 -2000 4.2 20 29 0.01]
   ["Guyana"                   786552 0.48 3786 4 196850 -6000 2.5 27 27 0.01]
   ["Bhutan"                   771608 1.12 8516 20 38117 320 2 28 46 0.01]
   ["Solomon Islands"          686884 2.55 17061 25 27990 -1600 4.4 20 23 0.01]
   ["Macao"                    649335 1.39 8890 21645 30 5000 1.2 39 nil 0.01]
   ["Montenegro"               628066 0.01 79 47 13450 -480 1.8 39 68 0.01]
   ["Luxembourg"               625978 1.66 10249 242 2590 9741 1.5 40 88 0.01]
   ["Western Sahara"           597339 2.55 14876 2 266000 5582 2.4 28 87 0.01]
   ["Suriname"                 586632 0.9 5260 4 156000 -1000 2.4 29 65 0.01]
   ["Cabo Verde"               555987 1.1 6052 138 4030 -1342 2.3 28 68 0.01]
   ["Maldives"                 540544 1.81 9591 1802 300 11370 1.9 30 35 0.01]
   ["Malta"                    441543 0.27 1171 1380 320 900 1.5 43 93 0.01]
   ["Brunei"                   437479 0.97 4194 83 5270 0 1.8 32 80 0.01]
   ["Guadeloupe"               400124 0.02 68 237 1690 -1440 2.2 44 nil 0.01]
   ["Belize"                   397628 1.86 7275 17 22810 1200 2.3 25 46 0.01]
   ["Bahamas"                  393244 0.97 3762 39 10010 1000 1.8 32 86 0.01]
   ["Martinique"               375265 -0.08 -289 354 1060 -960 1.9 47 92 0.0]
   ["Iceland"                  341243 0.65 2212 3 100250 380 1.8 37 94 0.0]
   ["Vanuatu"                  307145 2.42 7263 25 12190 120 3.8 21 24 0.0]
   ["French Guiana"            298682 2.7 7850 4 82200 1200 3.4 25 87 0.0]
   ["Barbados"                 287375 0.12 350 668 430 -79 1.6 40 31 0.0]
   ["New Caledonia"            285498 0.97 2748 16 18280 502 2 34 72 0.0]
   ["French Polynesia"         280908 0.58 1621 77 3660 -1000 2 34 64 0.0]
   ["Mayotte"                  272815 2.5 6665 728 375 0 3.7 20 46 0.0]
   ["Sao Tome & Principe"      219159 1.91 4103 228 960 -1680 4.4 19 74 0.0]
   ["Samoa"                    198414 0.67 1317 70 2830 -2803 3.9 22 18 0.0]
   ["Saint Lucia"              183627 0.46 837 301 610 0 1.4 34 19 0.0]
   ["Channel Islands"          173863 0.93 1604 915 190 1351 1.5 43 30 0.0]
   ["Guam"                     168775 0.89 1481 313 540 -506 2.3 31 95 0.0]
   ["Curaçao"                  164093 0.41 669 370 444 515 1.8 42 89 0.0]
   ["Kiribati"                 119449 1.57 1843 147 810 -800 3.6 23 57 0.0]
   ["Micronesia"               115023 1.06 1208 164 700 -600 3.1 24 21 0.0]
   ["Grenada"                  112523 0.46 520 331 340 -200 2.1 32 35 0.0]
   ["St. Vincent & Grenadines" 110940 0.32 351 284 390 -200 1.9 33 53 0.0]
   ["Aruba"                    106766 0.43 452 593 180 201 1.9 41 44 0.0]
   ["Tonga"                    105695 1.15 1201 147 720 -800 3.6 22 24 0.0]
   ["U.S. Virgin Islands"      104425 -0.15 -153 298 350 -451 2 43 96 0.0]
   ["Seychelles"               98347 0.62 608 214 460 -200 2.5 34 56 0.0]
   ["Antigua and Barbuda"      97929 0.84 811 223 440 0 2 34 26 0.0]
   ["Isle of Man"              85033 0.53 449 149 570 nil nil 53 0.0]
   ["Andorra"                  77265 0.16 123 164 470 nil nil 88 0.0]
   ["Dominica"                 71986 0.25 178 96 750 nil nil 74 0.0]
   ["Cayman Islands"           65722 1.19 774 274 240 nil nil 97 0.0]
   ["Bermuda"                  62278 -0.36 -228 1246 50 nil nil 97 0.0]
   ["Marshall Islands"         59190 0.68 399 329 180 nil nil 70 0.0]
   ["Northern Mariana Islands" 57559 0.6 343 125 460 nil nil 88 0.0]
   ["Greenland"                56770 0.17 98 0 410450 nil nil 87 0.0]
   ["American Samoa"           55191 -0.22 -121 276 200 nil nil 88 0.0]
   ["Saint Kitts & Nevis"      53199 0.71 376 205 260 nil nil 33 0.0]
   ["Faeroe Islands"           48863 0.38 185 35 1396 nil nil 43 0.0]
   ["Sint Maarten"             42876 1.15 488 1261 34 nil nil 96 0.0]
   ["Monaco"                   39242 0.71 278 26337 1 nil nil nil 0.0]
   ["Turks and Caicos"         38717 1.38 526 41 950 nil nil 89 0.0]
   ["Saint Martin"             38666 1.75 664 730 53 nil nil 0 0.0]
   ["Liechtenstein"            38128 0.29 109 238 160 nil nil 15 0.0]
   ["San Marino"               33931 0.21 71 566 60 nil nil 97 0.0]
   ["Gibraltar"                33691 -0.03 -10 3369 10 nil nil nil 0.0]
   ["British Virgin Islands"   30231 0.67 201 202 150 nil nil 52 0.0]
   ["Caribbean Netherlands"    26223 0.94 244 80 328 nil nil 75 0.0]
   ["Palau"                    18094 0.48 86 39 460 nil nil nil 0.0]
   ["Cook Islands"             17564 0.09 16 73 240 nil nil 75 0.0]
   ["Anguilla"                 15003 0.9 134 167 90 nil nil nil 0.0]
   ["Tuvalu"                   11792 1.25 146 393 30 nil nil 62 0.0]
   ["Wallis & Futuna"          11239 -1.69 -193 80 140 nil nil 0 0.0]
   ["Nauru"                    10824 0.63 68 541 20 nil nil nil 0.0]
   ["Saint Barthelemy"         9877 0.3 30 470 21 nil nil 0 0.0]
   ["Saint Helena"             6077 0.3 18 16 390 nil nil 27 0.0]
   ["Saint Pierre & Miquelon"  5794 -0.48 -28 25 230 nil nil 100 0.0]
   ["Montserrat"               4992 0.06 3 50 100 nil nil 10 0.0]
   ["Falkland Islands"         3480 3.05 103 0 12170 nil nil 66 0.0]
   ["Niue"                     1626 0.68 11 6 260 nil nil 46 0.0]
   ["Tokelau"                  1357 1.27 17 136 10 nil nil 0 0.0]
   ["Holy See"                 801 0.25 2 2003 0 nil nil nil 0.0]
   ]
  )

(def default_code "XX")

 ;; Mapping of country names to alpha-2 codes.
(def is_3166_1
  {
   "Afghanistan"                                  "AF"
   "Åland Islands"                                "AX"
   "Albania"                                      "AL"
   "Algeria"                                      "DZ"
   "American Samoa"                               "AS"
   "Andorra"                                      "AD"
   "Angola"                                       "AO"
   "Anguilla"                                     "AI"
   "Antarctica"                                   "AQ"
   "Antigua and Barbuda"                          "AG"
   "Argentina"                                    "AR"
   "Armenia"                                      "AM"
   "Aruba"                                        "AW"
   "Australia"                                    "AU"
   "Austria"                                      "AT"
   "Azerbaijan"                                   "AZ"
   "Bahamas"                                      "BS"
   "Bahrain"                                      "BH"
   "Bangladesh"                                   "BD"
   "Barbados"                                     "BB"
   "Belarus"                                      "BY"
   "Belgium"                                      "BE"
   "Belize"                                       "BZ"
   "Benin"                                        "BJ"
   "Bermuda"                                      "BM"
   "Bhutan"                                       "BT"
   "Bolivia, Plurinational State of"              "BO"
   "Bonaire, Sint Eustatius and Saba"             "BQ"
   "Bosnia and Herzegovina"                       "BA"
   "Botswana"                                     "BW"
   "Bouvet Island"                                "BV"
   "Brazil"                                       "BR"
   "British Indian Ocean Territory"               "IO"
   "Brunei Darussalam"                            "BN"
   "Bulgaria"                                     "BG"
   "Burkina Faso"                                 "BF"
   "Burundi"                                      "BI"
   "Cambodia"                                     "KH"
   "Cameroon"                                     "CM"
   "Canada"                                       "CA"
   "Cape Verde"                                   "CV"
   "Cayman Islands"                               "KY"
   "Central African Republic"                     "CF"
   "Chad"                                         "TD"
   "Chile"                                        "CL"
   "China"                                        "CN"
   "Christmas Island"                             "CX"
   "Cocos (Keeling) Islands"                      "CC"
   "Colombia"                                     "CO"
   "Comoros"                                      "KM"
   "Congo"                                        "CG"
   "Congo, the Democratic Republic of the"        "CD"
   "Cook Islands"                                 "CK"
   "Costa Rica"                                   "CR"
   "Côte d'Ivoire"                                "CI"
   "Croatia"                                      "HR"
   "Cuba"                                         "CU"
   "Curaçao"                                      "CW"
   "Cyprus"                                       "CY"
   "Czech Republic"                               "CZ"
   "Denmark"                                      "DK"
   "Djibouti"                                     "DJ"
   "Dominica"                                     "DM"
   "Dominican Republic"                           "DO"
   "Ecuador"                                      "EC"
   "Egypt"                                        "EG"
   "El Salvador"                                  "SV"
   "Equatorial Guinea"                            "GQ"
   "Eritrea"                                      "ER"
   "Estonia"                                      "EE"
   "Ethiopia"                                     "ET"
   "Falkland Islands (Malvinas)"                  "FK"
   "Faroe Islands"                                "FO"
   "Fiji"                                         "FJ"
   "Finland"                                      "FI"
   "France"                                       "FR"
   "French Guiana"                                "GF"
   "French Polynesia"                             "PF"
   "French Southern Territories"                  "TF"
   "Gabon"                                        "GA"
   "Gambia"                                       "GM"
   "Georgia"                                      "GE"
   "Germany"                                      "DE"
   "Ghana"                                        "GH"
   "Gibraltar"                                    "GI"
   "Greece"                                       "GR"
   "Greenland"                                    "GL"
   "Grenada"                                      "GD"
   "Guadeloupe"                                   "GP"
   "Guam"                                         "GU"
   "Guatemala"                                    "GT"
   "Guernsey"                                     "GG"
   "Guinea"                                       "GN"
   "Guinea-Bissau"                                "GW"
   "Guyana"                                       "GY"
   "Haiti"                                        "HT"
   "Heard Island and McDonald Islands"            "HM"
   "Holy See (Vatican City State)"                "VA"
   "Honduras"                                     "HN"
   "Hong Kong"                                    "HK"
   "Hungary"                                      "HU"
   "Iceland"                                      "IS"
   "India"                                        "IN"
   "Indonesia"                                    "ID"
   "Iran, Islamic Republic of"                    "IR"
   "Iraq"                                         "IQ"
   "Ireland"                                      "IE"
   "Isle of Man"                                  "IM"
   "Israel"                                       "IL"
   "Italy"                                        "IT"
   "Jamaica"                                      "JM"
   "Japan"                                        "JP"
   "Jersey"                                       "JE"
   "Jordan"                                       "JO"
   "Kazakhstan"                                   "KZ"
   "Kenya"                                        "KE"
   "Kiribati"                                     "KI"
   "Korea, Democratic People's Republic of"       "KP"
   "Korea, Republic of"                           "KR"
   "Kuwait"                                       "KW"
   "Kyrgyzstan"                                   "KG"
   "Lao People's Democratic Republic"             "LA"
   "Latvia"                                       "LV"
   "Lebanon"                                      "LB"
   "Lesotho"                                      "LS"
   "Liberia"                                      "LR"
   "Libya"                                        "LY"
   "Liechtenstein"                                "LI"
   "Lithuania"                                    "LT"
   "Luxembourg"                                   "LU"
   "Macao"                                        "MO"
   "North Macedonia"                              "MK"
   "Madagascar"                                   "MG"
   "Malawi"                                       "MW"
   "Malaysia"                                     "MY"
   "Maldives"                                     "MV"
   "Mali"                                         "ML"
   "Malta"                                        "MT"
   "Marshall Islands"                             "MH"
   "Martinique"                                   "MQ"
   "Mauritania"                                   "MR"
   "Mauritius"                                    "MU"
   "Mayotte"                                      "YT"
   "Mexico"                                       "MX"
   "Micronesia, Federated States of"              "FM"
   "Moldova, Republic of"                         "MD"
   "Monaco"                                       "MC"
   "Mongolia"                                     "MN"
   "Montenegro"                                   "ME"
   "Montserrat"                                   "MS"
   "Morocco"                                      "MA"
   "Mozambique"                                   "MZ"
   "Myanmar"                                      "MM"
   "Namibia"                                      "NA"
   "Nauru"                                        "NR"
   "Nepal"                                        "NP"
   "Netherlands"                                  "NL"
   "New Caledonia"                                "NC"
   "New Zealand"                                  "NZ"
   "Nicaragua"                                    "NI"
   "Niger"                                        "NE"
   "Nigeria"                                      "NG"
   "Niue"                                         "NU"
   "Norfolk Island"                               "NF"
   "Northern Mariana Islands"                     "MP"
   "Norway"                                       "NO"
   "Oman"                                         "OM"
   "Pakistan"                                     "PK"
   "Palau"                                        "PW"
   "Palestine, State of"                          "PS"
   "Panama"                                       "PA"
   "Papua New Guinea"                             "PG"
   "Paraguay"                                     "PY"
   "Peru"                                         "PE"
   "Philippines"                                  "PH"
   "Pitcairn"                                     "PN"
   "Poland"                                       "PL"
   "Portugal"                                     "PT"
   "Puerto Rico"                                  "PR"
   "Qatar"                                        "QA"
   "Réunion"                                      "RE"
   "Romania"                                      "RO"
   "Russian Federation"                           "RU"
   "Rwanda"                                       "RW"
   "Saint Barthélemy"                             "BL"
   "Saint Helena, Ascension and Tristan da Cunha" "SH"
   "Saint Kitts and Nevis"                        "KN"
   "Saint Lucia"                                  "LC"
   "Saint Martin (French part)"                   "MF"
   "Saint Pierre and Miquelon"                    "PM"
   "Saint Vincent and the Grenadines"             "VC"
   "Samoa"                                        "WS"
   "San Marino"                                   "SM"
   "Sao Tome and Principe"                        "ST"
   "Saudi Arabia"                                 "SA"
   "Senegal"                                      "SN"
   "Serbia"                                       "RS"
   "Seychelles"                                   "SC"
   "Sierra Leone"                                 "SL"
   "Singapore"                                    "SG"
   "Sint Maarten (Dutch part)"                    "SX"
   "Slovakia"                                     "SK"
   "Slovenia"                                     "SI"
   "Solomon Islands"                              "SB"
   "Somalia"                                      "SO"
   "South Africa"                                 "ZA"
   "South Georgia and the South Sandwich Islands" "GS"
   "South Sudan"                                  "SS"
   "Spain"                                        "ES"
   "Sri Lanka"                                    "LK"
   "Sudan"                                        "SD"
   "Suriname"                                     "SR"
   "Svalbard and Jan Mayen"                       "SJ"
   "Swaziland"                                    "SZ"
   "Sweden"                                       "SE"
   "Switzerland"                                  "CH"
   "Syrian Arab Republic"                         "SY"
   "Taiwan, Province of China"                    "TW"
   "Tajikistan"                                   "TJ"
   "Tanzania, United Republic of"                 "TZ"
   "Thailand"                                     "TH"
   "Timor-Leste"                                  "TL"
   "Togo"                                         "TG"
   "Tokelau"                                      "TK"
   "Tonga"                                        "TO"
   "Trinidad and Tobago"                          "TT"
   "Tunisia"                                      "TN"
   "Turkey"                                       "TR"
   "Turkmenistan"                                 "TM"
   "Turks and Caicos Islands"                     "TC"
   "Tuvalu"                                       "TV"
   "Uganda"                                       "UG"
   "Ukraine"                                      "UA"
   "United Arab Emirates"                         "AE"
   "United Kingdom"                               "GB"
   "United States"                                "US"
   "United States Minor Outlying Islands"         "UM"
   "Uruguay"                                      "UY"
   "Uzbekistan"                                   "UZ"
   "Vanuatu"                                      "VU"
   "Venezuela, Bolivarian Republic of"            "VE"
   "Viet Nam"                                     "VN"
   "Virgin Islands, British"                      "VG"
   "Virgin Islands, U.S."                         "VI"
   "Wallis and Futuna"                            "WF"
   "Western Sahara"                               "EH"
   "Yemen"                                        "YE"
   "Zambia"                                       "ZM"
   "Zimbabwe"                                     "ZW"
   })

;; Mapping of alternative names, spelling, typos to the names of countries used
;; by the ISO 3166-1 norm
(def synonyms
  {
   "Mainland China"   "China"
   "Czechia"          "Czech Republic"
   "South Korea"      "Korea Republic of"
   "Taiwan"           "Taiwan Province of China"
   "US"               "United States"
   "Macau"            "Macao"                       ;; TODO Macau is probably a typo. Report it to CSSEGISandData/COVID-19
   "Vietnam"          "Viet Nam"
   "UK"               "United Kingdom"
   "Russia"           "Russian Federation"
   "Iran"             "Iran Islamic Republic of"
   "Saint Barthelemy" "Saint Barthélemy"
   "Palestine"        "Palestine State of"
   "Vatican City"     "Holy See (Vatican City State)"

   "DR Congo"                 "Congo, the Democratic Republic of the"
   "Tanzania"                 "Tanzania, United Republic of"
   "Venezuela"                "Venezuela, Bolivarian Republic of"
   "North Korea"              "Korea, Democratic People's Republic of"
   "Syria"                    "Syrian Arab Republic"
   "Bolivia"                  "Bolivia, Plurinational State of"
   "Laos"                     "Lao People's Democratic Republic"
   "State of Palestine"       "Palestine State of"
   "Moldova"                  "Moldova, Republic of"
   "Eswatini"                 "Swaziland"
   "Cabo Verde"               "Cape Verde"
   "Brunei"                   "Brunei Darussalam"
   "Sao Tome & Principe"      "Sao Tome and Principe"
   "Micronesia"               "Micronesia, Federated States of"
   "St. Vincent & Grenadines" "Saint Vincent and the Grenadines"
   "U.S. Virgin Islands"      "Virgin Islands, U.S."
   "Saint Kitts & Nevis"      "Saint Kitts and Nevis"
   "Faeroe Islands"           "Faroe Islands"
   "Sint Maarten"             "Sint Maarten (Dutch part)"
   "Turks and Caicos"         "Turks and Caicos Islands"
   "Saint Martin"             "Saint Martin (French part)"
   "British Virgin Islands"   "Virgin Islands, British"
   "Wallis & Futuna"          "Wallis and Futuna"
   "Saint Helena"             "Saint Helena, Ascension and Tristan da Cunha"
   "Saint Pierre & Miquelon"  "Saint Pierre and Miquelon"
   "Falkland Islands"         "Falkland Islands (Malvinas)"
   "Holy See"                 "Holy See (Vatican City State)"

   ;; "Channel Islands" ;; https://en.wikipedia.org/wiki/Channel_Islands
   ;; "Caribbean Netherlands" ;; https://en.wikipedia.org/wiki/Caribbean_Netherlands
   ;; "Others" has no mapping
   })

(defn country_code
  "Return two letter country code (Alpha-2) according to
  https://en.wikipedia.org/wiki/ISO_3166-1
  Defaults to \"XX\"."
  [country]
  (if-let [cc (get is_3166_1 country)]
    cc
    (if-let [synonym (get synonyms country)]
      (get is_3166_1 synonym)
      (do
        #_country
        (println (str
                  "No country code found for '" country "'. Using '"
                  default_code "'"))
        default_code))))
