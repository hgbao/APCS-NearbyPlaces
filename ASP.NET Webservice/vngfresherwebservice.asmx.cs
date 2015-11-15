using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Net;
using System.IO;
using System.Web.Script.Serialization;

namespace VNGFresher
{
    [WebService(Namespace = "http://tempuri.org/")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [System.ComponentModel.ToolboxItem(false)]

    public class vngfresherwebservice : System.Web.Services.WebService
    {
        [WebMethod]
        public string HelloWorld()
        {
            return "Hello World";
        }

        private string getObject(string objectName, ref string str)
        {
            int start = str.IndexOf(objectName) - 1;
            if (start >= 0)
            {
                for (int end = start; end < str.Length; end++)
                {
                    if (str[end] == '\n')
                    {
                        string result = str.Substring(start, end - start);
                        str = str.Substring(end, str.Length - end);
                        return result;
                    }
                }

            }
            return "";
        }

        private string getDetail(string place_id, string key)
        {
            //Connect
            string url = "https://maps.googleapis.com/maps/api/place/details/json?";
            url = url + "placeid=" + place_id;
            url = url + "&key=" + key;
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url);
            HttpWebResponse response = (HttpWebResponse)request.GetResponse();

            //Get response json type
            String strResponse = "";
            StreamReader streamRead = new StreamReader(response.GetResponseStream());
            Char[] readBuff = new Char[256];
            int count = streamRead.Read(readBuff, 0, 256);
            while (count > 0)
            {
                strResponse += new String(readBuff, 0, count);
                count = streamRead.Read(readBuff, 0, 256);
            }

            //=== Get each data needed ===
            String result = "";
            //1. Address
            result =  result + getObject("formatted_address", ref strResponse);
            
            //2. Phone number
            result = result + getObject("formatted_phone_number", ref strResponse);
            
            //3. Latitude
            result = result + getObject("lat", ref strResponse);

            //4. Longitude
            result = result + getObject("lng", ref strResponse);
            result += ",";

            //5. Name
            result = result + getObject("name", ref strResponse);

            //6. Photo reference and convert to string after downloading the byte array
            result = result + getObject("photo_reference", ref strResponse);

            //7. Rating
            result = result + getObject("rating", ref strResponse);

            return result.Remove(result.Length - 1, 1);//remove ","
        }

        [WebMethod]
        public string getNearbyPlaces(string latitude, string longitude, string radius, string key)
        {
            //Connect
            string url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
            url = url + "location=" + latitude + "," + longitude;
            url = url + "&radius=" + radius;
            url = url + "&sensor=true";
            url = url + "&key=" + key;
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url);
            HttpWebResponse response = (HttpWebResponse)request.GetResponse();

            //Get response json type
            String strResponse = "";
            StreamReader streamRead = new StreamReader(response.GetResponseStream());
            Char[] readBuff = new Char[256];
            int count = streamRead.Read(readBuff, 0, 256);
            while (count > 0)
            {
                strResponse += new String(readBuff, 0, count);
                count = streamRead.Read(readBuff, 0, 256);
            }

            //=== Create result json string ===
            //1. Separate result into many places with only place_id as a jsonObject
            string[] list_place = new string[100];
            for (int i = 0; i < 100; i++)
            {
                int start = strResponse.IndexOf("place_id") - 1;
                if (start >= 0)
                {
                    for (int end = start; end < strResponse.Length; end++)
                    {
                        if (strResponse[end] == ',')
                        {
                            list_place[i] = strResponse.Substring(start, end - start);
                            strResponse = strResponse.Substring(end, strResponse.Length - end);
                            break;
                        }
                    }
                }
            }

            //2. Get detail data for each place
            for (int i = 0; i < 100; i++)
            {
                if (list_place[i] != null)
                {
                    int start = list_place[i].IndexOf(":") + 3;
                    String place_id = list_place[i].Substring(start, list_place[i].Length - start - 1);
                    list_place[i] = list_place[i] + "," + getDetail(place_id, key);
                }
            }

            //x. Concat all data
            string result = "{\"results\":[";
            for (int i = 0; i < 100; i++)
            {
                if (list_place[i] != null)
                {
                    result = result + "{" + list_place[i] + "}";
                    if (i + 1 < 100 && list_place[i + 1] != null)
                        result += ",";
                }
            }
            result = result + "]}";
            return result;
        }
    }
}
