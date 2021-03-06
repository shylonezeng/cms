 
(function ($) {

	WBDisplayObject = function ( thisElement, options ) {
		this.init( thisElement, options );
	}
	WBDisplayObject.prototype = 
	{
		defaults: { 
			fieldsPrefix: 'wb',
			customHandler: undefined
		},
		
		init: function ( thisElement, options ) {			
			this.thisElement = $(thisElement);
			this.options = $.extend ( {} , this.defaults, options );
		},
		getOptions: function () {
			if (! this.options) 
				return this.defaults
			else
				return this.options;
		},

		
		display: function (object) {
			var elements = this.thisElement.find('[id^="' + this.getOptions().fieldsPrefix + '"]');
			var tempThis = this;
			$.each( elements, function (index, value) {
				var key = $(value).attr('id').substring( tempThis.getOptions().fieldsPrefix.length );
				if (key in object) {
					var htmlField = ""; 
					if (tempThis.getOptions().customHandler) {
						htmlField = (tempThis.getOptions().customHandler)(key, object);
					} else {					
						htmlField = escapehtml(object[key]);
					}
					$(value).html(htmlField);
				}				
			})
		}
	};
	
	

$.fn.wbDisplayObject = function ( param ) {
		var $this = $(this),
		data = $this.data('wbDisplayObject');			
		var options = (typeof param == 'object') ? param : {} ; 
		if (!data) $this.data('wbDisplayObject', (data = new WBDisplayObject ($this, options)));	
		if (param == undefined) return data;
}	

}) (window.jQuery)