# SCP Foundatiom Core

### Testing

- Amazon purchases

1. Download `amazon.sdktester.json` from Amazon developer console

    It should contain something like:
    ```
    {
        "com.amazon.sample.iap.consumables.orange" : {
            "itemType": "CONSUMABLE",
            "price": 10.00,
            "title": "Orange",
            "description": "An orange",
            "smallIconUrl": "http://www.amazon.com/orange.jpg"
        },
        "com.amazon.sample.iap.subscriptions.mymagazine.month":
        {
          "description":"Monthly Subscription to My Magazine",
          "title":"My Magazine",
          "itemType":"SUBSCRIPTION",
          "price":5.0,
          "subscriptionParent":"com.amazon.sample.iap.subscriptions.mymagazine"
        }
    }
    ```
2. Push it to device via ADB to path `/mnt/sdcard/`: 

     `adb push [_Your_JSON_File_Folder_]/amazon.sdktester.json /mnt/sdcard/amazon.sdktester.json` 