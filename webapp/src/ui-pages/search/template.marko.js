module.exports = function create(__helpers) {
  var str = __helpers.s,
      empty = __helpers.e,
      notEmpty = __helpers.ne,
      _widgetAttrs = require("marko-widgets").attrs,
      __widget = require.resolve("./widget"),
      optimizer_taglib_page_tag = require("optimizer/taglib/page-tag"),
      _tag = __helpers.t,
      optimizer_taglib_head_tag = require("optimizer/taglib/head-tag"),
      marko_widgets_taglib_widget_tag = require("marko-widgets/taglib/widget-tag"),
      attr = __helpers.a,
      attrs = __helpers.as,
      browser_refresh_taglib_refresh_tag = require("browser-refresh-taglib/refresh-tag"),
      optimizer_taglib_body_tag = require("optimizer/taglib/body-tag"),
      marko_widgets_taglib_init_widgets_tag = require("marko-widgets/taglib/init-widgets-tag");

  return function render(data, out) {
    _tag(out,
      optimizer_taglib_page_tag,
      {
        "name": "search",
        "packagePath": "./optimizer.json",
        "dirname": __dirname
      });

    out.w(' <!doctype html> <html><head><meta charset="utf-8"><meta http-equiv="X-UA-Compatible" content="IE=edge"><meta name="viewport" content="width=device-width, initial-scale=1"><link href="/public/bootstrap/dist/css/bootstrap.css" type="text/css" rel="stylesheet"><link href="/public/jQRangeSlider-5.7.0/css/ithing.css" type="text/css" rel="stylesheet">');
    _tag(out,
      optimizer_taglib_head_tag,
      {});

    out.w('</head>');
    _tag(out,
      marko_widgets_taglib_widget_tag,
      {
        "module": __widget,
        "_cfg": data.widgetConfig
      },
      function(widget) {
        out.w('<body' +
          attr("id", widget.elId()) +
          attrs(_widgetAttrs(widget)) +
          '><div class="dg-head"><header><a href="/home">Digital Collections <span class="sub">by King Library Special Collections </span></a></header></div><div class="dg-body container-fluid"><div class="row"><div class="mainlft col-md-9"><div class="row"><div class="col-sm-12"><div class="dg-autofill"><input type="text" style="height: 50px; font-size: 20px;" class="form-control dg-src-col" id="srchcol" placeholder="Search the collection" autocomplete="off"><ul class="dg-auto"></ul></div></div></div><div class="row map-canvas-w"><div class="col-xs-12"><div id="map-canvas"></div></div></div><div class="row"><div class="col-xs-12"><div id="slider"></div></div></div><div id="cols-title" class="row"></div><div id="cols" class="row"><div class="col-xs-12"><div id="dg-docs"></div></div></div></div><div class="mainrt col-md-3"><div class="dg-rel"><div class="title">Related searches</div><ul></ul></div><div class="dg-rel-col"><div class="title">Filter by collections</div><ul></ul></div></div></div></div><footer>Powerd by OpenKingsAPI</footer>');
        _tag(out,
          browser_refresh_taglib_refresh_tag,
          {
            "enabled": true
          });

        out.w('<script src="/public/jquery/dist/jquery.js" type="text/javascript"></script><script src="/public/bootstrap/dist/js/bootstrap.js" type="text/javascript"></script> <script src="/public/jquery-ui/jquery-ui.js" type="text/javascript"></script> <script src="/public/pubsub.js" type="text/javascript"></script> <script src="/public/jQRangeSlider-5.7.0/jQAllRangeSliders-min.js" type="text/javascript"></script> <script src="/public/masonry/masonry.js" type="text/javascript"></script> <script src="/public/masonry/imagesloaded.js" type="text/javascript"></script> <script src="https://maps.googleapis.com/maps/api/js?v=3.exp"></script><script src="/public/maps/markerclusterer.js" type="text/javascript"></script><script src="/public/maps/maps-invoker.js" type="text/javascript"></script>');
        _tag(out,
          optimizer_taglib_body_tag,
          {});
        _tag(out,
          marko_widgets_taglib_init_widgets_tag,
          {});

        out.w('</body>');
      });

    out.w('</html>');
  };
}