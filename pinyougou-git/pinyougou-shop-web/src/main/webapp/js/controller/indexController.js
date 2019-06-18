app.controller('indexController',function ($scope,$controller,loginService) {
    //读取当前登录人
    $scope.showLoginName=function(){
        loginService.loginName().success(
            function (response) {
                //从后台获取到的是一个login map集合，取出key
                $scope.loginName=response.loginName;
        });
    }
});