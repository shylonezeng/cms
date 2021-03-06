var errorsGeneral = {
};

$().ready( function () {
	
	$('#wbAddParameterForm').wbObjectManager( { fieldsPrefix:'wba',
									  errorLabelsPrefix: 'erra',
									  errorGeneral:"errageneral",
									  errorLabelClassName: 'errorvalidationlabel',
									  errorInputClassName: 'errorvalidationinput',
									  fieldsDefaults: { overwriteFromUrl: 0, localeType: 0 },
									  validationRules: {
										'name': { rangeLength: { 'min': 1, 'max': 100 } }
									  }
									});
	$('#wbUpdateParameterForm').wbObjectManager( { fieldsPrefix:'wbu',
									  errorLabelsPrefix: 'erru',
									  errorGeneral:"errageneral",
									  fieldsDefaults: { overwriteFromUrl: 0, localeType: 0 },
									  errorLabelClassName: 'errorvalidationlabel',
									  errorInputClassName: 'errorvalidationinput',
									  validationRules: {
										'name': { rangeLength: { 'min': 1, 'max': 100 } }
									  }
									});

	$('#wbDeleteParameterForm').wbObjectManager( { fieldsPrefix: 'wbd',
									 errorGeneral:"errdgeneral",
									 errorLabelsPrefix: 'errd',
									 errorLabelClassName: 'errorvalidationlabel',
									} );							

	var tableDisplayHandler = function (fieldId, record) {
		if (fieldId=="_operations") {
			return '<a href="#" class="wbEditParameterClass" id="wbEditParam_' + escapehtml(record['key']) + '"><i class="icon-pencil"></i> Edit </a> | <a href="#" class="wbDeleteParameterClass" id="wbDelParam_' + escapehtml(record['key'])+ '"><i class="icon-trash"></i> Delete </a>'; 
		} else
		if (fieldId=="lastModified") {
			var date = new Date();
			return date.toFormatString(record[fieldId], "dd/mm/yyyy hh:mm:ss");
		}
	}
				
	$('#wbGlobalParamsTable').wbTable( { columns: [ {display: "Id", fieldId:"key"}, {display: "External ID", fieldId:"externalKey"}, {display: "Name", fieldId: "name"}, {display: "Value", fieldId: "value"},
									{display:"Last Modified", fieldId:"lastModified", customHandling:true, customHandler: tableDisplayHandler}, {display: "Edit/delete", fieldId:"_operations", customHandling:true, customHandler: tableDisplayHandler}],
						 keyName: "key",
						 tableBaseClass: "table table-condensed table-color-header",
						 paginationBaseClass: "pagination"
						});
		
			
	$('#wbAddParameterBtn').click ( function (e) {
		e.preventDefault();
		$('#wbAddParameterForm').wbObjectManager().resetFields();
		$('#wbAddParameterModal').modal('show');
	});
	
	var fSuccessAdd = function ( data ) {
		$('#wbAddParameterModal').modal('hide');
		$('#wbGlobalParamsTable').wbTable().insertRow(data);			
	}
	var fErrorAdd = function (errors, data) {
		$('#wbAddParameterForm').wbObjectManager().setErrors(errors);
	}

	$('.wbAddParameterBtnClass').click( function (e) {
		e.preventDefault();
		var errors = $('#wbAddParameterForm').wbObjectManager().validateFieldsAndSetLabels( errorsGeneral );
		if ($.isEmptyObject(errors)) {
			var parameter = $('#wbAddParameterForm').wbObjectManager().getObjectFromFields();
			parameter['ownerExternalKey'] = 0;
			var jsonText = JSON.stringify(parameter);
			$('#wbAddParameterForm').wbCommunicationManager().ajax ( { url: "./wbparameter",
															 httpOperation:"POST", 
															 payloadData:jsonText,
															 wbObjectManager : $('#wbAddParamaterForm').wbObjectManager(),
															 functionSuccess: fSuccessAdd,
															 functionError: fErrorAdd
															 } );
		}
	});

	var fSuccessUpdate = function ( data ) {
		$('#wbUpdateParameterModal').modal('hide');		
		$('#wbGlobalParamsTable').wbTable().updateRowWithKey(data,data["key"]);
	}
	var fErrorUpdate = function (errors, data) {
		$('#wbUpdateParameterForm').wbObjectManager().setErrors(errors);
	}

	$('.wbUpdateParameterBtnClass').click( function (e) {
		e.preventDefault();
		var errors = $('#wbUpdateParameterForm').wbObjectManager().validateFieldsAndSetLabels( errorsGeneral );
		if ($.isEmptyObject(errors)) {
			var object = $('#wbUpdateParameterForm').wbObjectManager().getObjectFromFields();
			object['ownerExternalKey'] = 0;
			var jsonText = JSON.stringify(object);
			$('#wbUpdateParameterForm').wbCommunicationManager().ajax ( { url: "./wbparameter/" + escapehtml(object['key']),
															 httpOperation:"PUT", 
															 payloadData:jsonText,
															 wbObjectManager : $('#wbUpdateParameterForm').wbObjectManager(),
															 functionSuccess: fSuccessUpdate,
															 functionError: fErrorUpdate
															 } );
		}
	});

	var fSuccessDelete = function ( data ) {
		$('#wbDeleteParameterModal').modal('hide');		
		$('#wbGlobalParamsTable').wbTable().deleteRowWithKey(data["key"]);
	}
	var fErrorDelete = function (errors, data) {
		$('#wbDeleteParameterForm').wbObjectManager().setErrors(errors);
	}

	$('.wbDeleteParameterBtnClass').click( function (e) {
		e.preventDefault();
		var errors = $('#wbDeleteParameterForm').wbObjectManager().validateFieldsAndSetLabels( errorsGeneral );
		if ($.isEmptyObject(errors)) {
			var object = $('#wbDeleteParameterForm').wbObjectManager().getObjectFromFields();
			$('#wbDeleteParameterForm').wbCommunicationManager().ajax ( { url: "./wbparameter/" + escapehtml(object['key']),
															 httpOperation:"DELETE", 
															 payloadData:"",
															 wbObjectManager : $('#wbDeleteParameterForm').wbObjectManager(),
															 functionSuccess: fSuccessDelete,
															 functionError: fErrorDelete
															 } );
		}
	});

	
	$(document).on ("click", ".wbEditParameterClass", function (e) {
		e.preventDefault();
		$('#wbUpdateParameterForm').wbObjectManager().resetFields();
		var key = $(this).attr('id').substring("wbEditParam_".length);
		var object = $('#wbGlobalParamsTable').wbTable().getRowDataWithKey(key);
		$('#wbUpdateParameterForm').wbObjectManager().populateFieldsFromObject(object);
		$('#wbUpdateParameterModal').modal('show');		
	});

	$(document).on ("click", ".wbDeleteParameterClass", function (e) {
		e.preventDefault();
		$('#wbDeleteParameterForm').wbObjectManager().resetFields();
		var key = $(this).attr('id').substring("wbDelParam_".length);
		var object = $('#wbGlobalParamsTable').wbTable().getRowDataWithKey(key);
		$('#wbDeleteParameterForm').wbObjectManager().populateFieldsFromObject(object);
		$('#wbDeleteParameterModal').modal('show');		
	});

	var fSuccessGetParameters = function (data) {
		$.each(data, function(index, item) {
			$('#wbGlobalParamsTable').wbTable().insertRow(item);
		});				

	}
	var fErrorGetParameters = function (errors, data) {
		alert(errors);
	}
	
	$('#wbAddParameterForm').wbCommunicationManager().ajax ( { url:"./wbparameter?ownerExternalKey=0",
													 httpOperation:"GET", 
													 payloadData:"",
													 functionSuccess: fSuccessGetParameters,
													 functionError: fErrorGetParameters
													} );

												
});