from django.shortcuts import render, redirect, get_object_or_404
from django.db.models import Q, Count
from .models import Post
from rest_framework import viewsets
from .serializers import PostSerializer

class IntruderImage(viewsets.ModelViewSet):
    queryset = Post.objects.all()
    serializer_class = PostSerializer

def post_list(request):
    query = request.GET.get('q', '').strip()
    posts = Post.objects.all().order_by('-created_date')
    if query:
        posts = posts.filter(Q(title__icontains=query) | Q(text__icontains=query))

    # 전체 검출 통계 계산 (편의기능 1: 클래스별 침입/검출 횟수 집계)
    all_posts = Post.objects.all()
    class_counts = {}
    for p in all_posts:
        if p.title:
            class_counts[p.title] = class_counts.get(p.title, 0) + 1

    return render(request, 'blog/post_list.html', {
        'posts': posts,
        'query': query,
        'total_count': all_posts.count(),
        'filtered_count': posts.count(),
        'class_counts': class_counts,
    })

def post_delete(request, pk):
    if request.method == 'POST':
        post = get_object_or_404(Post, pk=pk)
        post.delete()
    return redirect('post_list')