//商品详细页（控制层）
app.controller('itemController',function($scope,$http){
	//数量操作
	$scope.addNum=function(x){
		$scope.num=$scope.num+x;
		if($scope.num<1){
			$scope.num=1;
		}
	}

	$scope.specificationItems={};//记录用户选择的规格
	//用户选择规格
	$scope.selectSpecification=function(name,value){
		$scope.specificationItems[name]=value;
		searchSku();//读取sku
	}
	
	//判断某规格选项是否被用户选中
	$scope.isSelected=function(name,value){
		//如果是用户选择的，或者是没被选择的
		if($scope.specificationItems[name]==value){
			return true;
		}else{
			return false;
		}
	}
	
	//加载默认SKU
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec));//深克隆
	}
	
	//匹配两个对象:key:规格，value：规格选项
	
	matchObject=function(map1,map2){
		for(var key in map1){
			if(map1[key]!=map2[key]){
				return false;
			}
		}
		//两个都要遍历，防止集合长度不一致时，前面的元素一致
		for(var key in map2){
			if(map2[key]!=map1[key]){
				return false;
			}
		}
		return true;
	}
	
	//查询SKU
	searchSku=function(){
		for(var i=0;i<skuList.length;i++){
			//遍历规格选项，如果匹配则选中
			if( matchObject(skuList[i].spec ,$scope.specificationItems ) ){
				$scope.sku=skuList[i];
				return ;
			}
		}
		$scope.sku={id:0,title:'--------',price:0};//如果没有匹配的	
	}


	//添加商品到购物车
	$scope.addToCart=function(){
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+ $scope.sku.id +'&num='+$scope.num,{'withCredentials':true}).success(
            	function (response) {
					if(response.success){
						location.href='http://localhost:9107/cart.html';//跳转到购物车页面
					}else {
						alert(response.message);
					}
                }
		);
	}

	
});
