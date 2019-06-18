 //控制层 
app.controller('goodsController' ,function($scope,$controller ,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}


    // 查询实体
    $scope.findOne = function() {

        var id=$location.search()['id'];//获取参数值
        if(id==null){
            return;
        }
        goodsService.findOne(id).success(function(response) {
            $scope.entity = response;

            //向富文本编辑器添加商品介绍
            editor.html($scope.entity.goodsDesc.introduction);

            //显示图片列表
            $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);

            //显示扩展属性
            $scope.entity.goodsDesc.customAttributeItems=  JSON.parse($scope.entity.goodsDesc.customAttributeItems);

            //规格
            $scope.entity.goodsDesc.specificationItems= JSON.parse($scope.entity.goodsDesc.specificationItems);

            //sku列表规格列转换
            for(var i=0;i<$scope.entity.itemList.length;i++){
                $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
            }

        });
    }
    $scope.checkAttributeValue=function(specName,optionName){

        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items,'attributeName',specName);
        //object代表一个{"attributeName":"机身内存","attributeValue":["16G","32G"]}
        if(object!=null){
            if(object.attributeValue.indexOf(optionName)>=0){
                return true;
            }
        }else{
            return false;
        }
    }

    //保存
    $scope.save=function(){
        var serviceObject;//服务层对象
        if($scope.entity.id!=null){//如果有ID
            serviceObject=goodsService.update( $scope.entity ); //修改
        }else{
            serviceObject=goodsService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    //重新查询
                    $scope.reloadList();//重新加载
                }else{
                    alert(response.message);
                }
            }
        );
    }
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    $scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
    $scope.itemCatList=[];//商品分类列表
    //加载商品分类列表,遍历分类列表信息，将id赋给分类列表索引值,值为分类名
    $scope.findItemCatList=function () {
        itemCatService.findAll().success(
            function (response) {
                for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;
                }
            }
        );
    }
    //读取一级分类
    $scope.selectItemCatList=function(){
        itemCatService.findByParentId(0).success(function(response){
            $scope.itemCat1List=response;
        });
    }

    // 查询实体--运营商审核商品，详情功能，用模态框
    $scope.findOneShenHe = function(id) {


        goodsService.findOne(id).success(function(response) {
            $scope.entity = response;

            //向富文本编辑器添加商品介绍
            editor.html($scope.entity.goodsDesc.introduction);
            editor.readonly(true);
            //显示图片列表
            $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
            //显示扩展属性
            $scope.entity.goodsDesc.customAttributeItems=  JSON.parse($scope.entity.goodsDesc.customAttributeItems);
            //规格
            $scope.entity.goodsDesc.specificationItems= JSON.parse($scope.entity.goodsDesc.specificationItems);
            //sku列表规格列转换
            for(var i=0;i<$scope.entity.itemList.length;i++){
                $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
            }
            //根据一级，查询二级分类
            itemCatService.findByParentId($scope.entity.goods.category1Id).success(function(response){
                $scope.itemCat2List=response;
            });
            //根据二级，查询三级分类
            itemCatService.findByParentId($scope.entity.goods.category2Id).success(function(response){
                $scope.itemCat3List=response;
            });
            //根据三级级，查询模版级分类
            itemCatService.findOne($scope.entity.goods.category3Id).success(function(response){
                $scope.entity.goods.typeTemplateId=response.typeId;
            });
            //根据模版id查询品牌列表
            typeTemplateService.findOne($scope.entity.goods.typeTemplateId).success(function(response){
                $scope.typeTemplate=response;//获取类型模板
                $scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
            });
            //根据模版id查询规格列表
            typeTemplateService.findSpecList($scope.entity.goods.typeTemplateId).success(function(response){
                $scope.specList=response;
            });

        });
    }

    //更改状态
    $scope.updateStatus=function(status){
        goodsService.updateStatus($scope.selectIds,status).success(
            function(response){
                if(response.success){//成功
                    $scope.reloadList();//刷新列表
                    $scope.selectIds=[];//清空ID集合
                }else{
                    alert(response.message);
                }
            }
        );
    }



});	
