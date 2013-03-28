//
//  MapViewController.m
//  RentAHome
//
//  Created by Sasha Goldshtein on 3/24/13.
//  Copyright (c) 2013 Windows Azure. All rights reserved.
//

#import "MapViewController.h"
#import "ApartmentAnnotation.h"
#import <MapKit/MapKit.h>

@interface MapViewController ()

@property (weak, nonatomic) IBOutlet MKMapView *mapView;

@end

@implementation MapViewController

- (void)viewDidLoad {
    for (NSDictionary *apartment in self.apartments) {
        [self.mapView addAnnotation:[[ApartmentAnnotation alloc] initWithApartment:apartment]];
    }
}

@end
