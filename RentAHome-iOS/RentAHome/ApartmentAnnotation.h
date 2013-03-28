//
//  ApartmentAnnotation.h
//  RentAHome
//
//  Created by Sasha Goldshtein on 3/24/13.
//  Copyright (c) 2013 Windows Azure. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@interface ApartmentAnnotation : NSObject <MKAnnotation>

- (id)initWithApartment:(NSDictionary *)apartment;

@end
