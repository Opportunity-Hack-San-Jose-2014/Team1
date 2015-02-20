function Widget(config) {
	var filterData = {
		'keyword': ''
	};

	var keytype = false;

	/* var dummy = {
    "docs": [{
        "title": "Children in sanitarium.",
        "image": "http://digitalcollections.sjlibrary.org/utils/ajaxhelper/?CISOROOT=gordon&CISOPTR=1532&action=2&DMSCALE=10&DMWIDTH=350&DMHEIGHT=437&DMX=0&DMY=0&DMTEXT=hall&DMROTATE=0"
    }, {
        "title": "Agnews State Asylum for the Insane.",
        "image": "http://sjmusart.org/sites/default/files/styles/overlay/public/uploads/photos/5.3.c_history_0.jpg?itok=DYyS1a_N"
    }, {
        "title": "Remember when sandwiches cost a quarter?",
        "image": "http://upload.wikimedia.org/wikipedia/commons/9/91/San_Jose,_California,_South_First_Street,_1940s.jpg"
    }, {
        "title": "Ten thousand Beauty of Glazenwood Roses on a single bush.",
        "image": "http://www.bvnasj.org/SanJoseThenNow/ArtMuseum1910.jpg"
    }],
    "ack": "success",
    "years": {
        "yearmax": 0,
        "yearmin": 0
    },
    "autocomplete": ["Agnews State Asylum for the Insane.", "Remember when sandwiches cost a quarter?", "Ten thousand Beauty of Glazenwood Roses on a single bush.", "Children in sanitarium."],
    "suggestions": ["The Castro", "Mission District", "Mission San Fernando Rey de España", "Mission San Miguel Arcángel", "San Francisco Mint", "Mission San Gabriel Arcángel", "Mission San Juan Capistrano", "Mission San José", "Treasure Island", "Mission San Buenaventura", "San Francisco", "Mission San Francisco Solano", "Union Square", "Mission San Antonio de Padua", "Mission San Francisco de Asís", "Mission San Luis Rey de Francia", "Mission San Juan Bautista", "Mission San Diego de Alcalá", "Mission San Rafael Arcángel", "Mission San Carlos Borroméo del río Carmelo"],
    "collections": [{
        "name": "historic-photograph",
        "image": "http://upload.wikimedia.org/wikipedia/commons/1/1e/Stonehenge.jpg"
    }, {
        "name": "historic-photograph",
        "image": "http://upload.wikimedia.org/wikipedia/commons/1/1e/Stonehenge.jpg"
    }, {
        "name": "historic-photograph",
        "image": "http://upload.wikimedia.org/wikipedia/commons/1/1e/Stonehenge.jpg"
    }, {
        "name": "historic-photograph",
        "image": "http://upload.wikimedia.org/wikipedia/commons/1/1e/Stonehenge.jpg"
    }]
}; */

	var timer;

	//sliders
	$("#slider").rangeSlider(
		{
			bounds:{min: 1900, max: 2000},
			defaultValues:{min: 1920, max: 1980}
		}
	);

	filterData.year = '1920-1980';
	$("#slider").bind("valuesChanged", function(e, data){
		filterData.year = Math.round(data.values.min)+'-'+Math.round(data.values.max);
		$.publish('fetchdocs', filterData);
	});

	//gallery
	var $container = $('#dg-docs');
	$container.masonry({
		itemSelector: '.work-masonry-thumb'
	});

	//search
	$('#srchcol').on('keyup paste', function (e){
		var val = $(this).val();

		keytype = true;
		if (timer) {
			clearTimeout(timer);
		}

		timer = setTimeout(function(){
			$('.dg-auto').toggle(val && val.length != 0);

			//if (val && val.length != 0) {
				filterData.keyword = val;
				$.publish('fetchdocs', filterData);
				//$.publish('update.results', {'results': dummy.docs});
			//} else {
			//	$.publish('update.autocomplete', {});
			//	$.publish('update.suggestions', {});
			//}
		}, 500);
	});

	$('#srchcol').on('blur', function (e){
		$('.dg-auto').hide();
		keytype = false;
	});

	$.subscribe('update.autocomplete', function (e, data){
		var auto = data.autocomplete;
		var $ul = $('.dg-auto');

		$ul.html('');
		$ul.hide();
		if (auto && auto.length > 0 && keytype){
			$ul.show();
			var length = Math.min(auto.length, 5);
			for(var i=0; i<length; i++){
				$ul.append('<li><a href="#">'+auto[i]+'</a></li>');
			}
		}
	});

	$.subscribe('update.filter', function (e, data){
		//update map
		//update sliders
	});

	$.subscribe('update.results', function (e, data){
		var results = data.results;
		var $container = $('#dg-docs');
		var mapData = [];

		filterData.collection = '';
		filterData.location = '';
		$container.html('').hide();
		if (results && results.length > 0){
			$('#cols-title').html(results.length+' results found');
			for(var i=0; i<results.length; i++){
				$container.append('<div class="work-archive-thumb-link"><a href="/view?id='+results[i].id+'"><img class="work-masonry-thumb" title="'+results[i].title+'" alt="'+results[i].title+'" src="'+results[i].image+'"/></a></div>');

				mapData.push([results[i].title, results[i].lat, results[i].lon]);
			}
			// initialize Masonry after all images have loaded
			$container.imagesLoaded( function() {
			  $container.show();
			  $container.masonry('reloadItems');
			  $container.masonry();
			});

			gmapsns.data = mapData;
			gmapsns.init();
		} else {
			$('#cols-title').html('No results found');
			$container.html('');
		}
	});

	$.subscribe('update.suggestions', function (e, data){
		//update related search
		var suggestions = data.suggestions;
		var $ul = $('.dg-rel ul');

		$ul.html('');
		$('.dg-rel').hide();
		if (suggestions && suggestions.length > 0) {
			$('.dg-rel').show();
			for(var i=0; i<suggestions.length; i++){
				$ul.append('<li><a href="#">'+suggestions[i]+'</a></li>');
			}
		}
	});

	$.subscribe('update.collections', function (e, data){
		//update related collections
		var collections = data.collections;
		var $ul = $('.dg-rel-col ul');

		$ul.html('');
		$('.dg-rel-col').hide();
		if (collections && collections.length > 0) {
			$('.dg-rel-col').show();
			for(var i=0; i<collections.length; i++){
				$ul.append('<li><a href="#">'+'<img src="'+collections[i].image+'"/><div>'+collections[i].name+'</div></a></li>');

			}
		}
	});

	$(document).on('click', '.dg-auto a', function (e){
		e.preventDefault();
		$('#srchcol').val($(this).html());

		filterData.keyword = $('#srchcol').val();
		$.publish('fetchdocs', filterData);
	});

	$(document).on('click', '.dg-rel-col a', function (e){
		e.preventDefault();

		filterData.collection = $('div', $(this)).html();
		$.publish('fetchdocs', filterData);
	});

	$.publish('fetchdocs', filterData);
	
	$.subscribe('map.changed', function (e, data){
		console.log('map changed');
		filterData.location = data.lat+','+data.lon;
		$.publish('fetchdocs', filterData);
	});
}

module.exports = Widget;