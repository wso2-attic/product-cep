//TODO: enable timeout animations for map and sticky

var filterPath, scrollableElement, cc, range, scrollorama, responsivegadget;

$(document).ready(function() {
		
	filterPath = function(string) {
		return string.replace(/^\//, '').replace(/(index|default).[a-zA-Z]{3,4}$/, '').replace(/\/$/, '');
	}
	
	var locationPath = filterPath(location.pathname);

	scrollableElement = function(els) {
		for (var i = 0, argLength = arguments.length; i < argLength; i++) {
			var el = arguments[i], $scrollElement = $(el);
			if ($scrollElement.scrollTop() > 0) {
				return el;
			} else {
				$scrollElement.scrollTop(1);
				var isScrollable = $scrollElement.scrollTop() > 0;
				$scrollElement.scrollTop(0);
				if (isScrollable) {
					return el;
				}
			}
		}
		return [];
	}
	var scrollElem = scrollableElement('html', 'body');

	var animateFeatures = function() {

		scrollorama = scrollorama || $.scrollorama({
			blocks : '.scrollblock'
		});

		scrollorama.animate('#feature-2-store', {
			delay : 100,
			duration : 200,
			property : 'opacity',
			start : 0,
			end : 1
		});
		scrollorama.animate('#feature-2-store', {
			delay : 100,
			duration : 200,
			property : 'top',
			start : -400,
			end : 20
		});
	}
	
	
	/*----------------function calls---------------------*/


	$("#SliderSingle").slider({
		from : 1,
		to : 3,
		step : 1,
		round : 1,
		skin : "round_plastic",
		callback : function(value) {
			$("#SliderSingle").slider('value', value);

			updateIntroGadgets(value);

		}
	});
	$("#SliderSingle").slider('value', 1);

	$('a[href*=#]').each(function() {
		var thisPath = filterPath(this.pathname) || locationPath;
		if (locationPath == thisPath && (location.hostname == this.hostname || !this.hostname) && this.hash.replace(/#/, '')) {
			var $target = $(this.hash), target = this.hash;
			if (target) {
				var targetOffset = $target.offset().top;
				$(this).click(function(event) {
					event.preventDefault();
					$(scrollElem).animate({
						scrollTop : targetOffset
					}, 400, function() {
						location.hash = target;
					});
				});
			}
		}
	});

	animateFeatures();

});

