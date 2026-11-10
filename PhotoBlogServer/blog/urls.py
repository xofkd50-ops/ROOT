from django.urls import path, include
from . import views
from rest_framework import routers

router = routers.DefaultRouter()
router.register('Post', views.IntruderImage)

urlpatterns = [
    path('', views.post_list, name='post_list'),
    path('post/<int:pk>/delete/', views.post_delete, name='post_delete'),
    path('api_root/', include(router.urls)),
]