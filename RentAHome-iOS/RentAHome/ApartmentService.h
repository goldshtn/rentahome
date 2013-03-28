
#import <WindowsAzureMobileServices/WindowsAzureMobileServices.h>
#import <Foundation/Foundation.h>

#pragma mark * Block Definitions

typedef void (^CompletionBlock) ();
typedef void (^CompletionWithIndexBlock) (NSUInteger index);
typedef void (^BusyUpdateBlock) (BOOL busy);

#pragma mark * ApartmentService public interface

@interface ApartmentService : NSObject<MSFilter>

@property (nonatomic, strong)   NSArray *items;
@property (nonatomic, strong)   MSClient *client;
@property (nonatomic, copy)     BusyUpdateBlock busyUpdate;

+ (ApartmentService *)sharedService;

- (void)registerDeviceToken:(NSString *)token;

- (void)refreshDataOnSuccess:(CompletionBlock) completion;

- (void)addItem:(NSDictionary *) item
     completion:(CompletionWithIndexBlock) completion;

- (void)unpublishApartment: (NSDictionary *) item
                completion:(CompletionWithIndexBlock) completion;

- (void)handleRequest:(NSURLRequest *)request
               onNext:(MSFilterNextBlock)onNext
           onResponse:(MSFilterResponseBlock)onResponse;

@end
