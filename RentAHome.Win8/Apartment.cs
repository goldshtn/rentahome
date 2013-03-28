using Microsoft.WindowsAzure.MobileServices;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;
using System.Threading.Tasks;

namespace RentAHome.Win8
{
    [DataTable(Name = "apartment")]
    public class Apartment
    {
        public int Id { get; set; }

        [DataMember(Name = "address")]
        public string Address { get; set; }

        [DataMember(Name = "published")]
        public bool Published { get; set; }

        [DataMember(Name = "bedrooms")]
        public int Bedrooms { get; set; }

        [DataMember(Name = "latitude")]
        public double Latitude { get; set; }

        [DataMember(Name = "longitude")]
        public double Longitude { get; set; }

        [DataMember(Name = "username")]
        public string UserName { get; set; }
    }
}
