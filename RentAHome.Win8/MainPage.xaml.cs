using Bing.Maps;
using Microsoft.WindowsAzure.MobileServices;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.Networking.PushNotifications;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238

namespace RentAHome.Win8
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        public MainPage()
        {
            this.InitializeComponent();
            if (App.MobileService.CurrentUser != null)
            {
                btnLogin.Visibility = Visibility.Collapsed;
                txtLogin.Visibility = Visibility.Visible;
                txtLogin.Text = "Logged in using Twitter";
            }
            App.CurrentChannel.PushNotificationReceived += CurrentChannel_PushNotificationReceived;
            InitApartments();
        }

        async void CurrentChannel_PushNotificationReceived(PushNotificationChannel sender, PushNotificationReceivedEventArgs args)
        {
            await Dispatcher.RunAsync(CoreDispatcherPriority.Normal, InitApartments);
        }

        private async void InitApartments()
        {
            var apartments = await App.MobileService.GetTable<Apartment>()
                .Where(a => a.Published == true /*important to keep the '==true'*/)
                .ToListAsync();
            listApartments.ItemsSource = apartments;

            mapApartments.Children.Clear();
            foreach (Apartment apartment in apartments)
            {
                Pushpin pushpin = new Pushpin { Text = apartment.Bedrooms.ToString() };
                mapApartments.Children.Add(pushpin);
                Location location = new Location(apartment.Latitude, apartment.Longitude);
                MapLayer.SetPosition(pushpin, location);
                pushpin.Tapped += (s, e) =>
                {
                    mapApartments.SetView(location, 15);
                };
            }
        }

        /// <summary>
        /// Invoked when this page is about to be displayed in a Frame.
        /// </summary>
        /// <param name="e">Event data that describes how this page was reached.  The Parameter
        /// property is typically used to configure the page.</param>
        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
        }

        private async void Button_Click_1(object sender, RoutedEventArgs e)
        {
            Apartment apartment = new Apartment
            {
                Address = txtAddress.Text,
                Bedrooms = (int)sliderBedrooms.Value,
                Published = true
            };
            await App.MobileService.GetTable<Apartment>().InsertAsync(apartment);
        }

        private async void listApartments_ItemClick_1(object sender, ItemClickEventArgs e)
        {
            Apartment apartment = (Apartment)e.ClickedItem;
            Debug.WriteLine("Deleting apartment " + apartment.Address);
            await App.MobileService.GetTable<Apartment>().DeleteAsync(apartment);
            InitApartments();
        }

        private async void btnLogin_Click(object sender, RoutedEventArgs e)
        {
            var user = await App.MobileService.LoginAsync(MobileServiceAuthenticationProvider.Twitter);
            btnLogin.Visibility = Visibility.Collapsed;
            txtLogin.Visibility = Visibility.Visible;
            txtLogin.Text = "Logged in using Twitter";
        }
    }
}
