using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using RentAHome.WP8.Resources;
using Microsoft.Phone.Maps.Controls;
using System.Device.Location;
using System.Windows.Shapes;
using System.Windows.Media;
using System.Diagnostics;

namespace RentAHome.WP8
{
    public partial class MainPage : PhoneApplicationPage
    {
        // Constructor
        public MainPage()
        {
            InitializeComponent();
            InitApartments();

            App.CurrentChannel.ShellToastNotificationReceived += (s, e) =>
            {
                Debug.WriteLine("Shell toast notification received");
                Dispatcher.BeginInvoke(InitApartments);
            };
        }

        private async void InitApartments()
        {
            var apartments = await App.MobileService.GetTable<Apartment>().Where(a => a.Published == true).ToListAsync();
            listApartments.ItemsSource = apartments;

            mapApartments.Layers.Clear();
            MapLayer layer = new MapLayer();
            foreach (Apartment apartment in apartments)
            {
                MapOverlay overlay = new MapOverlay();
                overlay.GeoCoordinate = new GeoCoordinate(apartment.Latitude, apartment.Longitude);
                overlay.PositionOrigin = new Point(0, 0);
                Grid grid = new Grid { Height = 40, Width = 25, Background = new SolidColorBrush(Colors.Red) };
                TextBlock text = new TextBlock { Text = apartment.Bedrooms.ToString(), VerticalAlignment = VerticalAlignment.Center, HorizontalAlignment = HorizontalAlignment.Center };
                grid.Children.Add(text);
                overlay.Content = grid;
                grid.Tap += (s, e) =>
                {
                    MessageBox.Show(
                        "Address: " + apartment.Address + Environment.NewLine + apartment.Bedrooms + " bedrooms",
                        "Apartment", MessageBoxButton.OK);
                    mapApartments.SetView(overlay.GeoCoordinate, 15, MapAnimationKind.Parabolic);
                };
                layer.Add(overlay);
            }
            mapApartments.Layers.Add(layer);
        }

        private async void Button_Click_1(object sender, RoutedEventArgs e)
        {
            Apartment apartment = new Apartment
            {
                Address = txtAddress.Text,
                Bedrooms = (int)Math.Floor(sliderBedrooms.Value),
                Published = true
            };
            btnAdd.IsEnabled = false;
            await App.MobileService.GetTable<Apartment>().InsertAsync(apartment);
            btnAdd.IsEnabled = true;
        }
    }
}