var errorsGeneral = {
};

$().ready( function () {
	
	$('#wbArticleEditForm').wbObjectManager( { fieldsPrefix:'wbe',
									  errorLabelsPrefix: 'erre',
									  errorGeneral:"errageneral",
									  errorLabelClassName: 'errorvalidationlabel',
									  errorInputClassName: 'errorvalidationinput',
									 });

	var displayHandler = function (fieldId, record) {
		if (fieldId == 'lastModified') {
			var date = new Date();
			return date.toFormatString(record[fieldId], "dd/mm/yyyy hh:mm:ss");
		} 
		if (fieldId == 'title') {
			var innerHtml = '<a href="./webarticle.html?key=' + escapehtml(record['key']) + '">' + escapehtml(record['title']) + '</a>';
			return innerHtml;
		}

		return record[fieldId];
	}
	$('#wbArticleSummary').wbDisplayObject( { fieldsPrefix: 'wbsummary', customHandler: displayHandler} );
	
	var fSuccessGetArticle = function (data) {
		$('#wbArticleSummary').wbDisplayObject().display(data);
		$('#wbArticleEditForm').wbObjectManager().populateFieldsFromObject(data);
	}
	
	var fErrorGetArticle = function (errors, data) {
		alert(errors);
	}

	var pageKey = getURLParameter('key'); 
	$('#wbArticleEditForm').wbCommunicationManager().ajax ( { url:"./wbarticle/" + escapehtml(pageKey),
												 httpOperation:"GET", 
												 payloadData:"",
												 functionSuccess: fSuccessGetArticle,
												 functionError: fErrorGetArticle
												} );
	
	var fSuccessEdit = function ( data ) {
		window.location.href = "./webarticle.html?key=" + escapehtml(pageKey);
	}
	var fErrorEdit = function (errors, data) {
		$('#wbEditPageModuleForm').wbObjectManager().setErrors(errors);
	}

	$('.wbArticleEditSaveBtnClass').click( function (e) {
		e.preventDefault();
		var errors = $('#wbArticleEditForm').wbObjectManager().validateFieldsAndSetLabels( errorsGeneral );
		if ($.isEmptyObject(errors)) {
			var article = $('#wbArticleEditForm').wbObjectManager().getObjectFromFields();
			article['htmlSource'] = tinyMCE.get("wbehtmlSource").getContent();
			var jsonText = JSON.stringify(article);
			$('#wbArticleEditForm').wbCommunicationManager().ajax ( { url: "./wbarticle/" + escapehtml(pageKey),
															 httpOperation:"PUT", 
															 payloadData:jsonText,
															 wbObjectManager : $('#wbArticleEditForm').wbObjectManager(),
															 functionSuccess: fSuccessEdit,
															 functionError: fErrorEdit
															 } );
		}
	});
	
	$('.wbArticleEditCancelBtnClass').click ( function (e) {
		e.preventDefault();
		window.location.href = "./webarticle.html?key=" + escapehtml(pageKey);
	});


													
});