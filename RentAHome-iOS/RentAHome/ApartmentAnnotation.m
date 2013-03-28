//
//  ApartmentAnnotation.m
//  RentAHome
//
//  Created by Sasha Goldshtein on 3/24/13.
//  Copyright (c) 2013 Windows Azure. All rights reserved.
//

#import "ApartmentAnnotation.h"

@interface ApartmentAnnotation ()

@property (nonatomic, strong) NSDictionary *apartment;

@end

@implementation ApartmentAnnotation

- (id)initWithApartment:(NSDictionary *)apartment {
    if (self = [super init]) {
        self.apartment = apartment;
    }
    return self;
}

- (NSString *)title {
    return self.apartment[@"address"];
}

- (NSString *)subtitle {
    return [NSString stringWithFormat:@"%d bedrooms", [self.apartment[@"bedrooms"] integerValue]];
}

- (CLLocationCoordinate2D)coordinate {
    return CLLocationCoordinate2DMake([self.apartment[@"latitude"] doubleValue],
                                      [self.apartment[@"longitude"] doubleValue]);
}

@end
