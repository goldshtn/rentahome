Insert script on `channels` table:

```javascript
function insert(item, user, request) {
    var channelTable = tables.getTable('channels');
    channelTable
        .where({ uri: item.uri, type: item.type })
        .read({ success: insertChannelIfNotFound });

    function insertChannelIfNotFound(existingChannels) {
        if (existingChannels.length > 0) {
            request.respond(200, existingChannels[0]);
        } else {
            request.execute();
        }
    }
}
```

Insert script on `apartment` table:

```javascript
function insert(item, user, request) {
    var reqModule = require('request');
    
    item.username = '<Unknown User>';
    var identities = user ? user.getIdentities() : null;
    if (identities && identities.twitter) {
        var userId = user.userId;
        var twitterId = userId.substring(userId.indexOf(':') + 1);
        var userDetailsUrl = 'https://api.twitter.com/1/users/show.json?user_id=' + twitterId;
        reqModule(userDetailsUrl, function (err, resp, body) {
            if (err || resp.statusCode !== 200) {
                console.error('Error sending data to Twitter API: ' + err + ', response body: ' + body);
                request.respond(500, body);
            } else {
                try {
                    console.log('Twitter response: ' + body);
                    var userData = JSON.parse(body);
                    item.username = userData.name;
                    continueProcessing();
                } catch (ex) {
                    console.error('Error parsing response from Twitter API: ', ex);
                    request.respond(500, ex);
                }
            }
        });
    } else {
        continueProcessing();
    }
    
    function continueProcessing() {
        var what = escape(item.address);
        reqModule('http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=' + what,
        function(error, response, body) {
            if (error) {
                executeRequestAndSendNotifications();
            } else {
                var geoResult = JSON.parse(body);
                var location = geoResult.results[0].geometry.location;
                item.latitude = location.lat;
                item.longitude = location.lng;
                executeRequestAndSendNotifications();
            }
        });
        
        function executeRequestAndSendNotifications() {
            request.execute({
                success: function() {
                    request.respond();
                    sendNotifications();
                }
            });
        }
    
        function sendNotifications() {
            var channelTable = tables.getTable('channels');
            channelTable.read({
                success: function(channels) {
                    channels.forEach(function(channel) {
                        if (channel.type === "Windows 8") {
                            push.wns.sendToastText04(channel.uri, {
                                text1: 'New apartment added',
                                text2: item.address,
                                text3: item.bedrooms + ' bedrooms'
                            }, {
                                success: function(pushResponse) {
                                    console.log("Sent push notification: ", pushResponse);
                                }
                            });
                        }
                        if (channel.type === "Windows Phone 8") {
                            push.mpns.sendToast(channel.uri, 'New apartment added',
                                'At ' + item.address + ' with ' + item.bedrooms + ' bedrooms',
                                function (err) {
                                    console.log("Sent push notification: " + JSON.stringify(err));
                                });
                        }
                        if (channel.type === "iOS") {
                            push.apns.send(channel.uri, {
                                alert: "New " + item.bedrooms + "-bedrooms apartment added at " + item.address,
                                badge: 1,
                                payload: {
                                    address: item.address,
                                    bedrooms: item.bedrooms
                                }
                            });
                        }
                        if (channel.type === "Android") {
                            push.gcm.send(channel.uri,
                                {
                                    bedrooms: item.bedrooms.toString(),
                                    address: item.address
                                }, {
                                success: function(response) {
                                    console.log('Push notification sent: ', response);
                                }, error: function(error) {
                                    console.log('Error sending push notification: ', error);
                                }
                            });
                        }
                    });
                }
            });
        }
    }

}
```