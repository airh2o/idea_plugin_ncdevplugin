$(function(){
	//初始化左边页面,现在需要在页面中调用jtree_init
	//initleftside();

	//调用jtree
	//jtree_init();
	// 
	$("#searchTreeBtn").bind("click",function(){
	    if($("#searchTree").val()!=""){$("#searchTreeClose").show();}
	    $('ul.tree').colExpAll({clickType:'search'});
	});
	$("#searchTree").bind("keydown",function(e){
       if(e.keyCode==13){
           if($("#searchTree").val()!=""){$("#searchTreeClose").show();}else{$("#searchTreeClose").hide();};
           $('ul.tree').colExpAll({clickType:'search'})
       }
     });
	$("#searchTreeClose").bind("click",function(){
	    $("#searchTreeClose").hide();
	    $("#searchTree").val("");
	    $('ul.tree').colExpAll({clickType:'close'});
	});
})

function jtree_init(){
	var $p = $(document);
	$("ul.tree", $p).jTree();
	// $('div.accordion', $p).each(function(){
	// var $this = $(this);
	// 	$this.accordion({
	// 		fillSpace:$this.attr("fillSpace"),
	// 		alwaysOpen:false,
	// 		active:0
	// 	});
	// });
}

$.fn.extend({
	size:function(){
		return this.length;
	}	
})



