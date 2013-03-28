
#import "AppDelegate.h"
#import "ApartmentService.h"

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    [[UINavigationBar appearance] setTintColor:[UIColor lightGrayColor]];
    
    [[UIApplication sharedApplication] registerForRemoteNotificationTypes:UIRemoteNotificationTypeAlert|UIRemoteNotificationTypeBadge|UIRemoteNotificationTypeSound];
    [UIApplication sharedApplication].applicationIconBadgeNumber = 0;
    
    return YES;
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    NSCharacterSet *angleBrackets = [NSCharacterSet characterSetWithCharactersInString:@"<>"];
    NSString *token = [[deviceToken description] stringByTrimmingCharactersInSet:angleBrackets];
    NSLog(@"Registered with remote notifications device token: %@", token);
    [[ApartmentService sharedService] registerDeviceToken:token];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    NSLog(@"Failed to register for remote notifications, with error: %@", error);
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    NSLog(@"Received remote notification with user info: %@", userInfo);
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Notification"
                                                    message:[NSString stringWithFormat:@"New %@-bedroom apartment added at %@", userInfo[@"bedrooms"], userInfo[@"address"]]
                                                   delegate:nil
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
}

@end
