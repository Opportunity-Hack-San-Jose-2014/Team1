var gmapsns = {}

/**
 * Data for the markers consisting of a name, a LatLng and a zIndex for
 * the order in which these markers should display on top of each
 * other.
 */

gmapsns.data = [];

gmapsns.init = function (){
    var mapOptions = {
      zoom: 6,
      center: new google.maps.LatLng(36.778261, -119.4179324)
     }
    var map = new google.maps.Map(document.getElementById('map-canvas'),
                                  mapOptions);

    gmapsns.setMarkers(map, gmapsns.data, mapOptions);
}

gmapsns.setMarkers = function setMarkers(map, locations,mapOptions) {
    var image = {
      url: 'http://drorbn.net/AcademicPensieve/2011-12/DodecahedralLink-3.png',
      // This marker is 20 pixels wide by 32 pixels tall.
      size: new google.maps.Size(20, 32),
      // The origin for this image is 0,0.
      origin: new google.maps.Point(0,0),
      // The anchor for this image is the base of the flagpole at 0,32.
      anchor: new google.maps.Point(0, 32)
    };

   var shape = {
        coords: [1, 1, 1, 20, 18, 20, 18 , 1],
        type: 'poly'
    };

    var markerArray = [];

    var mc = new MarkerClusterer(map, [], mapOptions);

    for (var i = 0; i < locations.length; i++) {
      var beach = locations[i];
      var myLatLng = new google.maps.LatLng(beach[1], beach[2]);
      var marker = new google.maps.Marker({
          position: myLatLng,
          map: map,
          title: beach[0],
          zIndex: beach[3]
      });
      markerArray.push(marker);

      google.maps.event.addListener(marker, 'click', function(e) {
        console.log('on marker...');
        map.setZoom(16);
        map.setCenter(marker.getPosition());

        var pos = marker.getPosition();
        console.log(pos);
        $.publish('map.changed', {
          'lat': pos.k,
          'lon': pos.B
        });
      });
    }

    mc.addMarkers(markerArray, true);

    google.maps.event.addListener(mc, 'clusterclick', function(cluster) {
      console.log('on mc...');

      var pos = cluster.getCenter();
      console.log(pos);
      $.publish('map.changed', {
        'lat': pos.k,
        'lon': pos.B
      });
    });
}

google.maps.event.addDomListener(window, 'load', gmapsns.init);