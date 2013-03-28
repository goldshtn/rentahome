//
//  AddApartmentViewController.m
//  RentAHome
//
//  Created by Sasha Goldshtein on 3/24/13.
//  Copyright (c) 2013 Windows Azure. All rights reserved.
//

#import "AddApartmentViewController.h"

@interface AddApartmentViewController () <UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UITextField *itemText;
@property (weak, nonatomic) IBOutlet UISegmentedControl *bedrooms;

@end

@implementation AddApartmentViewController

- (IBAction)saveTapped:(id)sender {
    NSDictionary *apartment = @{ @"address" : self.itemText.text,
                                 @"bedrooms" : @(self.bedrooms.selectedSegmentIndex+1),
                                 @"published" : @(YES)
                                 };
    if ([self.delegate respondsToSelector:@selector(saveApartment:)]) {
        [self.delegate performSelector:@selector(saveApartment:) withObject:apartment];
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

@end
