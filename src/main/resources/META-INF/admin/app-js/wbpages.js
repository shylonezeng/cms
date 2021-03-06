var errorsGeneral = {
};

$().ready( function () {
	$('#wbAddPageForm').wbObjectManager( { fieldsPrefix:'wba',
									  errorLabelsPrefix: 'erra',
									  errorGeneral:"errageneral",
									  errorLabelClassName: 'errorvalidationlabel',
									  errorInputClassName: 'errorvalidationinput',
									  fieldsDefaults: { isTemplateSource: 0 },
									  validationRules: {
										'name': { rangeLength: { 'min': 1, 'max': 100 } }
									  }
									});
	$('#wbDuplicatePageForm').wbObjectManager( { fieldsPrefix:'wbc',
								  errorLabelsPrefix: 'errc',
								  errorGeneral:"errcgeneral",
								  errorLabelClassName: 'errorvalidationlabel',
								  errorInputClassName: 'errorvalidationinput',
								  fieldsDefaults: { isTemplateSource: 0 },
								  validationRules: {
									'name': { rangeLength: { 'min': 1, 'max': 100 } }
								  }
								});

	$('#wbDeletePageForm').wbObjectManager( { fieldsPrefix: 'wbd',
									 errorGeneral:"errdgeneral",
									 errorLabelsPrefix: 'errd',
									 errorLabelClassName: 'errorvalidationlabel',
									} );							

	var displayHandler = function (fieldId, record) {
		if (fieldId=="_operations") {
			return '<a href="./webpage.html?key=' + escapehtml(record['key']) + '&externalKey=' + escapehtml(record['externalKey']) + '"><i class="icon-pencil"></i> Edit </a>' + 
				 '| <a href="#" class="wbDeletePageClass" id="wbDeletePage_' +record['key']+ '"><i class="icon-trash"></i> Delete </a>' +
				 '| <a href="#" class="wbDuplicatePageClass" id="wbDuplicatePage_' +record['key']+ '"><i class="aicon-duplicate"></i> Duplicate </a>'; 
		} else
		if (fieldId=="lastModified") {
			var date = new Date();
			return date.toFormatString(record[fieldId], "dd/mm/yyyy hh:mm:ss");
		}
	}
				
	$('#wbPagesTable').wbTable( { columns: [ {display: "Id", fieldId:"key"}, {display: "External Id", fieldId:"externalKey"}, {display: "Name", fieldId: "name"}, 
									{display:"Last Modified", fieldId:"lastModified", customHandling: true, customHandler: displayHandler}, {display: "Edit/delete", fieldId:"_operations", customHandling:true, customHandler: displayHandler}],
						 keyName: "key",
						 tableBaseClass: "table table-condensed table-color-header",
						 paginationBaseClass: "pagination"
						});

	$('#wbAddPageForm').wbCommunicationManager();
	$('#wbDeletePageForm').wbCommunicationManager();

	$('#wbAddPageBtn').click( function (e) {
		e.preventDefault();
		$('#wbAddPageForm').wbObjectManager().resetFields();
		$('#wbAddPageModal').modal('show');
	});

	var fromOwnerExternalKey = 0;
	
	var fSuccessAdd = function ( data ) {
		$('#wbAddPageModal').modal('hide');
		$('#wbPagesTable').wbTable().insertRow(data);			
	}
	var fErrorAdd = function (errors, data) {
		$('#wbAddPageForm').wbObjectManager().setErrors(errors);
	}

	$('.wbSaveAddPageBtnClass').click( function (e) {
		e.preventDefault();
		var errors = $('#wbAddPageForm').wbObjectManager().validateFieldsAndSetLabels( errorsGeneral );
		if ($.isEmptyObject(errors)) {
			var jsonText = JSON.stringify($('#wbAddPageForm').wbObjectManager().getObjectFromFields());
			$('#wbAddPageForm').wbCommunicationManager().ajax ( { url: "./wbpage",
															 httpOperation:"POST", 
															 payloadData:jsonText,
															 wbObjectManager : $('#wbAddPageForm').wbObjectManager(),
															 functionSuccess: fSuccessAdd,
															 functionError: fErrorAdd
															 } );
		}
	});

	var fSuccessDuplicateParams = function ( data ) {
		$('#wbDuplicatePageModal').modal('hide');
	};
	var fErrorDuplicateParams = function (errors, data) {
		alert(errors);
	};

	var fSuccessDuplicate = function ( data ) {
		$('#wbPagesTable').wbTable().insertRow(data);	
		var fromOwnerExternalKey = $('#wbcexternalKey').val();
		var ownerExternalKey = data['externalKey'];
		$('#wbDuplicatePageForm').wbCommunicationManager().ajax ( { url: "./wbparameter?fromOwnerExternalKey={0}&ownerExternalKey={1}".format(fromOwnerExternalKey, ownerExternalKey),
															 httpOperation:"POST", 
															 payloadData:"",
															 wbObjectManager : $('#wbDuplicatePageForm').wbObjectManager(),
															 functionSuccess: fSuccessDuplicateParams,
															 functionError: fErrorDuplicateParams
															 } );		
	};
	var fErrorDuplicate = function (errors, data) {
		$('#wbDuplicatePageForm').wbObjectManager().setErrors(errors);
	};

	$('.wbSaveDuplicatePageBtnClass').click( function (e) {
		e.preventDefault();
		var errors = $('#wbDuplicatePageForm').wbObjectManager().validateFieldsAndSetLabels( errorsGeneral );
		if ($.isEmptyObject(errors)) {
			var object = $('#wbDuplicatePageForm').wbObjectManager().getObjectFromFields();
			delete object['externalKey'];
			var jsonText = JSON.stringify(object);
			$('#wbDuplicatePageForm').wbCommunicationManager().ajax ( { url: "./wbpage",
															 httpOperation:"POST", 
															 payloadData:jsonText,
															 wbObjectManager : $('#wbDuplicatePageForm').wbObjectManager(),
															 functionSuccess: fSuccessDuplicate,
															 functionError: fErrorDuplicate
															 } );
		}
	});

	$(document).on ("click", '.wbDuplicatePageClass', function (e) {
		e.preventDefault();
		$('#wbDuplicatePageForm').wbObjectManager().resetFields();
		var key = $(this).attr('id').substring("wbDuplicatePage_".length);
		var object = $('#wbPagesTable').wbTable().getRowDataWithKey(key);
		$('#wbDuplicatePageForm').wbObjectManager().populateFieldsFromObject(object);
		$('#wbDuplicatePageModal').modal('show');		
	});

	$(document).on ("click", '.wbDeletePageClass', function (e) {
		e.preventDefault();
		$('#wbDeletePageForm').wbObjectManager().resetFields();
		var key = $(this).attr('id').substring("wbDeletePage_".length);
		var object = $('#wbPagesTable').wbTable().getRowDataWithKey(key);
		$('#wbDeletePageForm').wbObjectManager().populateFieldsFromObject(object);
		$('#wbDeletePageModal').modal('show');		
	});

	var fSuccessDelete = function ( data ) {
		$('#wbDeletePageModal').modal('hide');	
		$('#wbPagesTable').wbTable().deleteRowWithKey(data["key"]);
	}
	var fErrorDelete = function (errors, data) {
		$('#wbDeletePageForm').wbObjectManager().setErrors(errors);
	}

	$('.webSaveDeleteBtnClass').click( function (e) {
		e.preventDefault();
		var object = $('#wbDeletePageForm').wbObjectManager().getObjectFromFields();			
		$('#wbDeletePageForm').wbCommunicationManager().ajax ( { url: "./wbpage/" + escapehtml(object['key']),
														 httpOperation:"DELETE", 
														 payloadData:"",
														 functionSuccess: fSuccessDelete,
														 functionError: fErrorDelete
													} );
		
	});

	var fSuccessGetPages = function (data) {
		$.each(data, function(index, item) {
			$('#wbPagesTable').wbTable().insertRow(item);
		});				

	}
	var fErrorGetPages = function (errors, data) {
	
	}
	
	$('#wbAddPagesForm').wbCommunicationManager().ajax ( { url:"./wbpage",
													 httpOperation:"GET", 
													 payloadData:"",
													 functionSuccess: fSuccessGetPages,
													 functionError: fErrorGetPages
													} );

});