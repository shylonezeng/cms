
var errorsGeneral = {
	'ERROR_URI_START_CHAR': "Web relative url must to start with /",
	'ERROR_URI_LENGTH': 'Web relative url length must be between 1 and 250 characters',
	'ERROR_PAGENAME_LENGTH': 'Web page name length must be between 1 and 250 characters ',
	'ERROR_CONTROLLER_LENGTH': 'Controller class length must be maximum 250 characters',
	'ERROR_INVALID_HTTP_OPERATION': 'Operation not valid, allowed values: GET, PUT, DELETE, POST',
	'ERROR_URI_BAD_FORMAT':'Invalid url format: allowed characters are 0-9, a-z, A-Z,-,_,~,. (, is not an allowed character)',
	'ERROR_CONTROLLER_BAD_FORMAT': 'Invalid format for controller class: allowed characters are 0-9, a-z, A-Z, -, _, (, is not an allowed character)',
	'ERROR_PAGE_BAD_FORMAT': 'Invalid format for page name: allowed characters are 0-9, a-z, A-Z, -, _, (, is not an allowed character)'
	
};

$().ready( function () {
	var wbUriValidationRules = {
								'uri': [ {rule:{startsWith: '/'}, error: 'ERROR_URI_START_CHAR'}, {rule:{customRegexp:{pattern:"^/([0-9a-zA-Z_~.-]*(\{[0-9a-zA-Z_.*-]+\})*[0-9a-zA-Z_~.-]*/?)*$", modifiers:"gi"}}, error:"ERROR_URI_BAD_FORMAT"}, { rule:{rangeLength: { 'min': 1, 'max': 250 } }, error:"ERROR_URI_LENGTH"} ],
								'pageName': [ { rule: { rangeLength: { 'min': 1, 'max': 250 } }, error:"ERROR_PAGENAME_LENGTH" }, {rule:{customRegexp:{pattern:"^[0-9a-zA-Z_.-]*$", modifiers:"gi"}}, error:"ERROR_PAGE_BAD_FORMAT"}],
								'controllerClass': [{ rule:{ maxLength: 250 }, error: "ERROR_CONTROLLER_LENGTH"}, {rule:{customRegexp:{pattern:"^[0-9a-zA-Z_.-]*$", modifiers:"gi"}}, error:"ERROR_CONTROLLER_BAD_FORMAT"}],
								'httpOperation': [{ rule: {includedInto: ['GET', 'POST', 'PUT', 'DELETE']}, error: "ERROR_INVALID_HTTP_OPERATION" }]
							  };
	$('#wburiadd').wbObjectManager( { fieldsPrefix:'wba',
									  errorLabelsPrefix: 'erra',
									  errorGeneral:"errageneral",
									  errorLabelClassName: 'errorvalidationlabel',
									  errorInputClassName: 'errorvalidationinput',
									  fieldsDefaults: { 'uri': '/', 'httpOperation': 'GET', enabled: 0 },
									  validationRules: wbUriValidationRules

									});

	$('#wburiduplicate').wbObjectManager( { fieldsPrefix:'wbc',
								  errorLabelsPrefix: 'errc',
								  errorGeneral:"errcgeneral",
								  errorLabelClassName: 'errorvalidationlabel',
								  errorInputClassName: 'errorvalidationinput',
								  fieldsDefaults: { 'uri': '/', 'httpOperation': 'GET', enabled: 0 },
								  validationRules: wbUriValidationRules
								});

	$('#wburiupdate').wbObjectManager( { fieldsPrefix:'wbu',
									  errorLabelsPrefix: 'erru',
									  errorGeneral:"errugeneral",
									  errorLabelClassName: 'errorvalidationlabel',
									  errorInputClassName: 'errorvalidationinput',
									  fieldsDefaults: { 'uri': '/', 'httpOperation': 'GET', enabled: 0 },
									  validationRules: wbUriValidationRules
									});
	$('#wburidelete').wbObjectManager( { fieldsPrefix: 'wbd',
										 errorGeneral:"errdgeneral",
										 errorLabelsPrefix: 'errd',
										 errorLabelClassName: 'errorvalidationlabel',
									    } );							
	$('#wbsearch').wbSearchBox( { searchFields: ['key','uri'] } );
	
	var displayHandler = function (fieldId, record) {
		if (fieldId=="_operations") {
			return '<a href="#" class="wbedituri" id="wburiedit_' +record['key']+ '"><i class="icon-pencil"></i> Edit </a> | <a href="#" class="wbdeleteuri" id="wburidel_' +record['key']+ '"><i class="icon-trash"></i> Delete </a>' 
					+ '| <a href="#" class="wbduplicateuri" id="wburidup_' + record['key']+ '"><i class="aicon-duplicate"></i> Duplicate </a>'; 
		} else
		if (fieldId=="lastModified") {
			var date = new Date();
			date.setTime(record[fieldId]);
			return "{0}/{1}/{2} {3}:{4}:{5}".format(date.getDate(), date.getMonth()+1, date.getFullYear(), date.getHours(), date.getMinutes(), date.getSeconds());
		}
	}
	
	$('#wbtable').wbTable( { columns: [ {display: "Id", fieldId:"key"}, {display: "External Id", fieldId:"externalKey"}, {display: "uri", fieldId: "uri"}, {display: "Page Name", fieldId: "pageName"}, {display: "Live", fieldId: "enabled"}, 
	                                    {display:"Operations", fieldId:"httpOperation"}, {display:"Controller class", fieldId:"controllerClass"}, 
										{display:"Last Modified", fieldId:"lastModified", customHandling:true, customHandler: displayHandler}, {display: "Edit/delete", fieldId:"_operations", customHandling:true, customHandler: displayHandler}],
							 keyName: "key",
							 tableBaseClass: "table table-condensed table-color-header",
							 paginationBaseClass: "pagination"
							});
	$('#wbtable').wbTable().addNotifier($('#wbsearch').wbSearchBox().changeListener, $('#wbsearch').wbSearchBox());	
	
	
	var fSuccessAdd = function ( data ) {
		$('#wbModalUriAdd').modal('hide');
		$('#wbtable').wbTable().insertRow(data);			
	};
	var fErrorAdd = function (errors, data) {
		var om = $('#wburiadd').wbObjectManager();
		om.setErrors( om.convertErrors(errors, errorsGeneral));
	};

	var fSuccessDuplicate = function ( data ) {
		$('#wbModalUriDuplicate').modal('hide');
		$('#wbtable').wbTable().insertRow(data);			
	};
	var fErrorDuplicate = function (errors, data) {
		$('#wburiduplicate').wbObjectManager().setErrors(errors);
	};

	var fSuccessUpdate = function ( data ) {
		$('#wbModalUriUpdate').modal('hide');		
		$('#wbtable').wbTable().updateRowWithKey(data,data["key"]);
	};
	var fErrorUpdate = function (errors, data) {
		$('#wburiupdate').wbObjectManager().setErrors(errors);
	};

	var fSuccessDelete = function ( data ) {
		$('#wbModalUriDelete').modal('hide');	
		$('#wbtable').wbTable().deleteRowWithKey(data["key"]);
	};
	var fErrorDelete = function (errors, data) {
		$('#wburidelete').wbObjectManager().setErrors(errors);
	};

	$('#wburiadd').wbCommunicationManager();
	$('#wburiupdate').wbCommunicationManager(); 
	$('#wburidelete').wbCommunicationManager(); 
	
	$('#wbaddnewpage').click( function (e) {
		e.preventDefault();
		$('#wburiadd').wbObjectManager().resetFields();
		$('#wbModalUriAdd').modal('show');
	});
	
	$(document).on ("click", ".wbedituri", function (e) {
		e.preventDefault();
		$('#wburiupdate').wbObjectManager().resetFields();
		var key = $(this).attr('id').substring("wburiedit_".length);
		var object = $('#wbtable').wbTable().getRowDataWithKey(key);
		$('#wburiupdate').wbObjectManager().populateFieldsFromObject(object);
		$('#wbModalUriUpdate').modal('show');		
	});

	$(document).on ("click", ".wbduplicateuri", function (e) {
		e.preventDefault();
		$('#wburiduplicate').wbObjectManager().resetFields();
		var key = $(this).attr('id').substring("wburidup_".length);
		var object = $('#wbtable').wbTable().getRowDataWithKey(key);
		$('#wburiduplicate').wbObjectManager().populateFieldsFromObject(object);
		$('#wbModalUriDuplicate').modal('show');		
	});

	$(document).on ("click", '.wbdeleteuri', function (e) {
		e.preventDefault();
		$('#wburidelete').wbObjectManager().resetFields();
		var key = $(this).attr('id').substring("wburidel_".length);
		var object = $('#wbtable').wbTable().getRowDataWithKey(key);
		$('#wburidelete').wbObjectManager().populateFieldsFromObject(object);
		$('#wbModalUriDelete').modal('show');		
	});
	
	$('.uriAddSave').click( function (e) {
		e.preventDefault();
		var errors = $('#wburiadd').wbObjectManager().validateFieldsAndSetLabels( errorsGeneral );
		if ($.isEmptyObject(errors)) {
			var jsonText = JSON.stringify($('#wburiadd').wbObjectManager().getObjectFromFields());
			$('#wburiadd').wbCommunicationManager().ajax ( { url: "./wburi",
															 httpOperation:"POST", 
															 payloadData:jsonText,
															 wbObjectManager : $('#wburiadd').wbObjectManager(),
															 functionSuccess: fSuccessAdd,
															 functionError: fErrorAdd
															 } );
		}
	});

	$('.uriDuplicateSave').click( function (e) {
		e.preventDefault();
		var errors = $('#wburiduplicate').wbObjectManager().validateFieldsAndSetLabels( errorsGeneral );
		if ($.isEmptyObject(errors)) {
			var jsonText = JSON.stringify($('#wburiduplicate').wbObjectManager().getObjectFromFields());
			$('#wburiduplicate').wbCommunicationManager().ajax ( { url: "./wburi",
															 httpOperation:"POST", 
															 payloadData:jsonText,
															 wbObjectManager : $('#wburiduplicate').wbObjectManager(),
															 functionSuccess: fSuccessDuplicate,
															 functionError: fErrorDuplicate
															 } );
		}
	});

	$('.uriUpdateSave').click( function (e) {
		e.preventDefault();
		var errors = $('#wburiupdate').wbObjectManager().validateFieldsAndSetLabels( errorsGeneral );
		if ($.isEmptyObject(errors)) {
			var object = $('#wburiupdate').wbObjectManager().getObjectFromFields();
			var jsonText = JSON.stringify(object);
			$('#wburiupdate').wbCommunicationManager().ajax ( { url: "./wburi/" + escapehtml(object['key']),
															 httpOperation:"PUT", 
															 payloadData:jsonText,
															 wbObjectManager : $('#wburiupdate').wbObjectManager(),
															 functionSuccess: fSuccessUpdate,
															 functionError: fErrorUpdate
															 } );
		}
	});

	$('.uriDeleteSave').click( function (e) {
		e.preventDefault();
		var object = $('#wburidelete').wbObjectManager().getObjectFromFields();			
		$('#wburidelete').wbCommunicationManager().ajax ( { url: "./wburi/" + escapehtml(object['key']),
														 httpOperation:"DELETE", 
														 payloadData:"",
														 functionSuccess: fSuccessDelete,
														 functionError: fErrorDelete
													} );
		
	});

	var fSuccessGetUris = function (data) {
		$.each(data, function(index, item) {
			$('#wbtable').wbTable().insertRow(item);
		});				

	}
	var fErrorGetUris = function (errors, data) {
	
	}
	
	$('#wburiadd').wbCommunicationManager().ajax ( { url:"./wburi",
													 httpOperation:"GET", 
													 payloadData:"",
													 functionSuccess: fSuccessGetUris,
													 functionError: fErrorGetUris
													} );
			
});