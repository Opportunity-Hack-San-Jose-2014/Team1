var searchClient = {};

searchClient.getDocs = function (params){

	console.log(params);
	if (searchClient.ajax) {
		searchClient.ajax.abort();
	}

	searchClient.ajax = $.ajax({
		url: 'http://localhost:8080/ohack2014/spendaday/v1/search',
		data: params,
        type: 'get',
	}).done(function (d){
		$.publish('update.suggestions', {'suggestions': d.suggestions});
		$.publish('update.autocomplete', {'autocomplete': d.autocomplete});
		$.publish('update.results', {'results': d.docs});
		$.publish('update.collections', {'collections': d.collections});
		$.publish('update.filters', {
			'years': d.years
		});
	});
};

$.subscribe('fetchdocs', function (e, data){
	searchClient.getDocs(data);
});
