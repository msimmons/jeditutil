package net.contrapt.jeditutil

import spock.lang.Specification

/**
 * Created by mark on 6/25/15.
 */
class JSONUtilSpec extends Specification {
    
    def json = """
{"_id":
   {
      "_time": 1366151950,
      "_machine": -1319750898,
      "_inc": 1767306696,
      "_new": false
         },
           "srNumber": "SR70090423170",
           "baseCost": 515,
           "costComponent": {
           "name": "PRICE_EXPERIMENT",
           "amount": 0
         },
           "paymentInfo": {
            "type": "charity",
            "source": "CHARITY",
            "sourceId": 16,
            "name": "Michelle Williams",
            "address1": "999 Fake St.",
            "address2": "",
            "city": "Stamford",
            "state": "CT",
            "zip": "06902",
            "country": "United States",
            "isCharity": true,
            "payableTo": "Sarah Morris",
            "srNumber": ["SR1","SR2", "SR3"]
           },
           "dbCollection": "itemAccepted",
           "headers": {
             "destination": "queue://erp.item",
             "routeId": "mongoLogger",
             "exchangeId": "ID-ip-10-195-210-235-53987-1365798169660-4-1705",
             "create_dt": "2013-04-16 22:39:10 GMT"
           }
         }
"""
    def "Convert the json to xml"() {

        when:
        JSONUtil jsonUtil = new JSONUtil()

        then:
        println jsonUtil.json2xml(json)
    }
}
