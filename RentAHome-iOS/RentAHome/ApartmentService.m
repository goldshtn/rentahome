

#import "ApartmentService.h"
#import <WindowsAzureMobileServices/WindowsAzureMobileServices.h>

#pragma mark * Private interace

@interface ApartmentService()

@property (nonatomic, strong)   MSTable *table;
@property (nonatomic)           NSInteger busyCount;

@end

#pragma mark * Implementation

@implementation ApartmentService

static ApartmentService *service;

+ (ApartmentService *)sharedService {
    if (!service) {
        service = [[ApartmentService alloc] init];
    }
    return service;
}

- (void)registerDeviceToken:(NSString *)token {
    MSTable *channelsTable = [self.client getTable:@"channels"];
    NSDictionary *channel = @{ @"uri" : token, @"type" : @"iOS" };
    [channelsTable insert:channel completion:^(NSDictionary *item, NSError *error) {
        if (error) {
            NSLog(@"Error inserting device token to mobile table: %@", error);
        }
    }];
}

- (ApartmentService *)init
{
    MSClient *newClient = [MSClient clientWithApplicationURLString:@"https://rentahome.azure-mobile.net/"
                                                withApplicationKey:@"VvqfVLoQPInuOScAVsQuHIxhSrbGmI34"];
    
    // Add a Mobile Service filter to enable the busy indicator
    self.client = [newClient clientwithFilter:self];
    
    // Create an MSTable instance to allow us to work with the apartment table
    self.table = [_client getTable:@"apartment"];
    
    self.items = [[NSMutableArray alloc] init];
    self.busyCount = 0;
    
    return self;
}

- (void)refreshDataOnSuccess:(CompletionBlock)completion
{
    // Create a predicate that finds apartments that are still published
    NSPredicate * predicate = [NSPredicate predicateWithFormat:@"published == YES"];
    
    // Query the apartment table and update the items property with the results from the service
    [self.table readWhere:predicate completion:^(NSArray *results, NSInteger totalCount, NSError *error) {
        [self logErrorIfNotNil:error];
        
        self.items = [results mutableCopy];
        
        // Let the caller know that we finished
        completion();
    }];

}

- (void)addItem:(NSDictionary *)item completion:(CompletionWithIndexBlock)completion
{
    // Insert the item into the apartment table and add to the items array on completion
    [self.table insert:item completion:^(NSDictionary *result, NSError *error) {
        
        [self logErrorIfNotNil:error];
        
        NSUInteger index = [self.items count];
        if (result) {
            [(NSMutableArray *)self.items insertObject:result atIndex:index];
            completion(index);
        } else {
            completion(-1);
        }
    }];
}

- (void)unpublishApartment:(NSDictionary *)item completion:(CompletionWithIndexBlock)completion
{
    // Cast the public items property to the mutable type (it was created as mutable)
    NSMutableArray *mutableItems = (NSMutableArray *) self.items;
    
    // Set the item to be unpublished (we need a mutable copy)
    NSMutableDictionary *mutable = [item mutableCopy];
    [mutable setObject:@(NO) forKey:@"published"];
    
    // Replace the original in the items array
    NSUInteger index = [self.items indexOfObjectIdenticalTo:item];
    [mutableItems replaceObjectAtIndex:index withObject:mutable];
    
    // Update the item in the apartment table and remove from the items array on completion
    [self.table update:mutable completion:^(NSDictionary *item, NSError *error) {
        
        [self logErrorIfNotNil:error];
        
        NSUInteger index = [self.items indexOfObjectIdenticalTo:mutable];
        [mutableItems removeObjectAtIndex:index];
        
        // Let the caller know that we have finished
        completion(index);
    }];
}

- (void) busy:(BOOL) busy
{
    // assumes always executes on UI thread
    if (busy) {
        if (self.busyCount == 0 && self.busyUpdate != nil) {
            self.busyUpdate(YES);
        }
        self.busyCount ++;
    }
    else
    {
        if (self.busyCount == 1 && self.busyUpdate != nil) {
            self.busyUpdate(FALSE);
        }
        self.busyCount--;
    }
}

- (void) logErrorIfNotNil:(NSError *) error
{
    if (error) {
        NSLog(@"ERROR %@", error);
    }
}

#pragma mark * MSFilter methods

- (void) handleRequest:(NSURLRequest *)request
                onNext:(MSFilterNextBlock)onNext
            onResponse:(MSFilterResponseBlock)onResponse
{
    if ([request HTTPBody]) {
        NSLog(@"%@ %@: %@, %s", [request HTTPMethod], request, [request allHTTPHeaderFields], (const char*)[[request HTTPBody] bytes]);
    }
    
    // A wrapped response block that decrements the busy counter
    MSFilterResponseBlock wrappedResponse = ^(NSHTTPURLResponse *response, NSData *data, NSError *error) {
        [self busy:NO];
        onResponse(response, data, error);
    };
    
    // Increment the busy counter before sending the request
    [self busy:YES];
    onNext(request, wrappedResponse);
}

@end
