mod-facebook
========================
mod-facebook uses the facebook query language to access data from facebook. See the [fql documentation](https://developers.facebook.com/docs/technical-guides/fql/).


Default config:

    {
      "address"  : "de.orolle.vertx2.modfacebook", // eventbus address
      "http-timeout" : 10000 // in ms
    }


# FQL

At the moment this module only supports fql. See the [fql documentation](https://developers.facebook.com/docs/technical-guides/fql/).


# Open Graph API

This module does not support the Open Graph API from facebook to access the open graph.
The Open Graph api is planed in future versions.

# Access Token

This module needs an access token from facebook. 
For testing you can get an access token from [Facebooks API Explorer](https://developers.facebook.com/tools/explorer?method=GET&path=me) easily. The access token is receivable through the OAuth authentification, too.
Be aware the token could time out and needs enough rights to access the data. 
Last but not least facebooks documentation is your friend.

## FQL

Use FQL to get data from facebook.

### Inputs

    {
      action: "fql",
      accesstoken: "<FACEBOOK ACCESS TOKEN>",
      query: {
                "myName": "SELECT name, uid FROM user WHERE uid = me()",
                "myUrl": "SELECT url FROM url_like WHERE user_id IN (SELECT uid FROM #myName)"
              }
    }

### OUTPUTS

    {
      "myName":[
        {
          "name":"Prename Surname",
          "uid":1234567
        }
      ],
      "myUrl":[
        {
          "url":"https://vertx.io/"
        },
        {
          "url":"https://www.facebook.com/"
        }
      ]
    }

or

    {
      error: <message>
    }

