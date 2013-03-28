
#import <WindowsAzureMobileServices/WindowsAzureMobileServices.h>
#import "HomeController.h"
#import "ApartmentService.h"

#pragma mark * Private Interface

@interface HomeController () <UIActionSheetDelegate>

@property (strong, nonatomic) ApartmentService *apartmentService;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *activityIndicator;

@end

#pragma mark * Implementation

@implementation HomeController

#pragma mark * UIView methods

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self.refreshControl addTarget:self
                            action:@selector(didPullRefreshControl)
                  forControlEvents:UIControlEventValueChanged];
    
    self.apartmentService = [ApartmentService sharedService];

    // Set the busy method 
    UIActivityIndicatorView *indicator = self.activityIndicator;
    self.apartmentService.busyUpdate = ^(BOOL busy) {
        if (busy) {
            [indicator startAnimating];
        } else {
            [indicator stopAnimating];
        }
    };
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self didPullRefreshControl];
}

- (void)didPullRefreshControl {
    [self.refreshControl beginRefreshing];
    [self.apartmentService refreshDataOnSuccess:^{
        [self.tableView reloadData];
    }];
    [self.refreshControl endRefreshing];
}

- (IBAction)showActions:(id)sender {
    UIActionSheet *sheet = [[UIActionSheet alloc] initWithTitle:@"What would you like to do?"
                                                       delegate:self
                                              cancelButtonTitle:@"Cancel"
                                         destructiveButtonTitle:nil
                                              otherButtonTitles:@"Login", @"Show Map", nil];
    [sheet showInView:self.view];
}

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (buttonIndex == actionSheet.firstOtherButtonIndex) {
        [self login];
    } else if (buttonIndex == actionSheet.firstOtherButtonIndex+1) {
        [self performSegueWithIdentifier:@"Show Map" sender:self];
    }
}

- (void)login
{
    UIViewController *controller = [self.apartmentService.client
     loginViewControllerWithProvider:@"twitter"
     completion:^(MSUser *user, NSError *error) {
         if (error) {
             NSLog(@"Authentication Error: %@", error);
             // error.code == -1503 means user cancelled the dialog
         } else {
             [self.apartmentService refreshDataOnSuccess:^{
                 [self.tableView reloadData];
             }];
         }
         [self dismissViewControllerAnimated:YES completion:nil];
     }];

    [self presentViewController:controller animated:YES completion:nil];    
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([segue.identifier isEqualToString:@"Add Apartment"]) {
        if ([segue.destinationViewController respondsToSelector:@selector(setDelegate:)]) {
            [segue.destinationViewController performSelector:@selector(setDelegate:) withObject:self];
        }
    }
    if ([segue.identifier isEqualToString:@"Show Map"]) {
        if ([segue.destinationViewController respondsToSelector:@selector(setApartments:)]) {
            [segue.destinationViewController performSelector:@selector(setApartments:)
                                                  withObject:self.apartmentService.items];
        }
    }
}

#pragma mark * UITableView methods


- (void) tableView:(UITableView *)tableView
                commitEditingStyle:(UITableViewCellEditingStyle)editingStyle
                 forRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Find item that was commited for editing (unpublished)
    NSDictionary *item = [self.apartmentService.items objectAtIndex:indexPath.row];
    
    // Ask the todoService to set the item's published value to NO, and remove the row if successful
    [self.apartmentService unpublishApartment:item completion:^(NSUInteger index) {
        
        // Remove the row from the UITableView
        NSIndexPath *indexPath = [NSIndexPath indexPathForRow:index inSection:0];
        [self.tableView deleteRowsAtIndexPaths:@[ indexPath ]
                              withRowAnimation:UITableViewRowAnimationTop];
    }];
}

-(UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Find the item that is about to be edited
    NSDictionary *item = [self.apartmentService.items objectAtIndex:indexPath.row];
    
    // If the item is unpublished, then this is just pending upload. Editing is not allowed
    if ([[item objectForKey:@"published"] boolValue] == NO) {
        return UITableViewCellEditingStyleNone;
    }
    
    // Otherwise, allow the delete button to appear
    return UITableViewCellEditingStyleDelete;
}

-(NSString *)tableView:(UITableView *)tableView titleForDeleteConfirmationButtonForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Customize the Delete button to say "unpublish"
    return @"Unpublish";
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
    
    NSDictionary *apartment = [self.apartmentService.items objectAtIndex:indexPath.row];
    cell.textLabel.text = apartment[@"address"];
    cell.detailTextLabel.text = [NSString stringWithFormat:@"%d bedrooms", [apartment[@"bedrooms"] integerValue]];
    
    return cell;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Always a single section
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of items in the todoService items array
    return [self.apartmentService.items count];
}

#pragma mark * UITextFieldDelegate methods


-(BOOL) textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}

#pragma mark * UI Actions

- (void)saveApartment:(NSDictionary *)apartment
{
    [self.navigationController popViewControllerAnimated:YES];

    __weak HomeController *weakSelf = self;
    [self.apartmentService addItem:apartment completion:^(NSUInteger index){
        if (index == -1) {
            return;
        }
        NSIndexPath *indexPath = [NSIndexPath indexPathForRow:index inSection:0];
        [weakSelf.tableView insertRowsAtIndexPaths:@[ indexPath ]
                              withRowAnimation:UITableViewRowAnimationTop];
    }];
}

@end
