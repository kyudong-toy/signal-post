import { useState } from 'react';
import {
  Heart,
  MessageCircle,
  Calendar,
  Grid,
  Bookmark,
  Eye,
  Play,
  UserPlus, Settings, Trash2,
} from 'lucide-react';
import {Card} from "@shared/ui/card.tsx";
import {Avatar, AvatarFallback, AvatarImage} from "@shared/ui/avatar.tsx";
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "@shared/ui/dialog.tsx";
import {Button} from "@shared/ui/button.tsx";

interface Post {
  id: number;
  imageUrl: string;
  likeCount: number;
  commentCount: number;
  viewCount: number;
  type: 'image' | 'video';
}

interface UserProfile {
  userId: number;
  username: string;
  displayName: string;
  bio: string;
  profileImage: string;
  backgroundImage: string;
  followerCount: number;
  followingCount: number;
  postCount: number;
  createdAt: string;
  isFollowing: boolean;
}

const UserProfileCard = () => {
  const [activeTab, setActiveTab] = useState<'posts' | 'saved'>('posts');
  const [isFollowing, setIsFollowing] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    displayName: 'ㅋㅋㅋㅋ',
    intro: 'ㅇㅇㅇㅇ',
    profileImage: null as File | null,
    backgroundImage: null as File | null,
  });

  // 샘플 데이터
  const profile: UserProfile = {
    userId: 1,
    username: "creative_artist",
    displayName: "크리에이티브 아티스트",
    bio: "🎨 디지털 아티스트 & 콘텐츠 크리에이터\n✨ 매일 새로운 작품을 선보입니다\n📍 서울, 대한민국\n💌 협업 문의 환영합니다",
    profileImage: "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=200&h=200&fit=crop&crop=face",
    backgroundImage: "https://images.unsplash.com/photo-1557804506-669a67965ba0?w=800&h=200&fit=crop",
    followerCount: 45200,
    followingCount: 1205,
    createdAt: "2022년 8월",
    isFollowing: false
  };

  const posts: Post[] = [
    { id: 1, imageUrl: "https://images.unsplash.com/photo-1501594907352-04cda38ebc29?w=300&h=300&fit=crop", likeCount: 1250, commentCount: 89, viewCount: 5420, type: 'image' },
    { id: 2, imageUrl: "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=300&h=300&fit=crop", likeCount: 2100, commentCount: 156, viewCount: 8930, type: 'video' },
    { id: 3, imageUrl: "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=300&h=300&fit=crop", likeCount: 890, commentCount: 67, viewCount: 3210, type: 'image' },
    { id: 4, imageUrl: "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=300&h=300&fit=crop", likeCount: 3400, commentCount: 234, viewCount: 12800, type: 'video' },
    { id: 5, imageUrl: "https://images.unsplash.com/photo-1542273917363-3b1817f69a2d?w=300&h=300&fit=crop", likeCount: 1890, commentCount: 123, viewCount: 6750, type: 'image' },
    { id: 6, imageUrl: "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=300&h=300&fit=crop", likeCount: 2650, commentCount: 189, viewCount: 9870, type: 'image' }
  ];

  const formatNumber = (num: number) => {
    if (num >= 10000) return `${(num / 10000).toFixed(1)}만`;
    if (num >= 1000) return `${(num / 1000).toFixed(1)}k`;
    return num.toString();
  };

  const handleFollow = () => setIsFollowing(!isFollowing);

  const handleEditProfile = () => setIsEditModalOpen(true);

  const handleSaveProfile = () => {
    // API 호출로 프로필 및 파일 업데이트
    console.log('Save profile:', formData);
    setIsEditModalOpen(false);
  };

  const handleDeleteImage = (type: 'profileImage' | 'backgroundImage') => {
    // API 호출로 이미지 삭제
    console.log(`Delete ${type}`);
    setFormData({ ...formData, [type]: null });
  };


  return (
    <div className="min-h-screen flex justify-center p-6">
      <Card className="max-w-2xl w-full bg-white rounded-2xl shadow-sm border border-gray-200">
        {/* 배경 이미지 */}
        <div className="relative h-48 bg-gradient-to-r from-blue-400 to-purple-500 rounded-t-2xl overflow-hidden">
          <img
            src={profile.backgroundImage}
            alt="Background"
            className="w-full h-full object-cover"
          />
          <div className="absolute inset-0 bg-black bg-opacity-20"></div>
        </div>

        <div className="px-8 pb-8">
          {/* 프로필 정보 섹션 */}
          <div className="flex items-end justify-between -mt-16 mb-6">
            {/* 프로필 이미지 */}
            <div className="relative">
              <Avatar className="w-32 h-32 rounded-full border-4 border-white shadow-lg">
                <AvatarImage src={profile.profileImage} alt={profile.username} />
                <AvatarFallback>{profile.username.substring(0, 2)}</AvatarFallback>
              </Avatar>
            </div>

            {/* 액션 버튼들 */}
            <div className="flex space-x-3 mb-4">
              <button
                onClick={handleFollow}
                className={`px-6 py-2 rounded-lg font-medium transition-colors ${
                  isFollowing
                    ? 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                    : 'bg-blue-500 text-white hover:bg-blue-600'
                }`}
              >
                <div className="flex items-center space-x-2">
                  <UserPlus size={16} />
                  <span>{isFollowing ? '팔로잉' : '팔로우'}</span>
                </div>
              </button>
              <Button
                onClick={handleEditProfile}
                className="top-4 right-4 p-2 bg-opacity-30 hover:bg-opacity-50 rounded-full transition-colors">
                <Settings size={20} />
              </Button>
            </div>
          </div>

          {/* 사용자 정보 */}
          <div className="mb-6">
            <div className="flex items-center space-x-2 mb-2">
              <h1 className="text-2xl font-bold text-gray-900">{profile.displayName}</h1>
            </div>
            <p className="text-gray-600 mb-4">@{profile.username}</p>

            <div className="whitespace-pre-line text-gray-800 mb-4 leading-relaxed">
              {profile.bio}
            </div>

            {/* 부가 정보 */}
            <div className="flex flex-wrap items-center text-sm text-gray-500 space-x-4 mb-4">
              <div className="flex items-center space-x-1">
                <Calendar size={14} />
                <span>{profile.createdAt} 가입</span>
              </div>
            </div>

            {/* 팔로워/팔로잉 통계 */}
            <div className="flex space-x-6">
              <div className="text-center hover:bg-gray-50 px-3 py-1 rounded transition-colors">
                <div className="text-xl font-bold text-gray-900">{formatNumber(profile.followerCount)}</div>
                <div className="text-sm text-gray-500">팔로워</div>
              </div>
              <div className="text-center hover:bg-gray-50 px-3 py-1 rounded transition-colors">
                <div className="text-xl font-bold text-gray-900">{formatNumber(profile.followingCount)}</div>
                <div className="text-sm text-gray-500">팔로잉</div>
              </div>
            </div>
          </div>

          {/* 탭 네비게이션 */}
          <div className="border-b border-gray-200 mb-6">
            <div className="flex space-x-8">
              <button
                onClick={() => setActiveTab('posts')}
                className={`py-3 px-1 border-b-2 font-medium text-sm transition-colors ${
                  activeTab === 'posts'
                    ? 'border-blue-500 text-blue-500'
                    : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
              >
                <div className="flex items-center space-x-2">
                  <Grid size={16} />
                  <span>게시물</span>
                </div>
              </button>
              <button
                onClick={() => setActiveTab('saved')}
                className={`py-3 px-1 border-b-2 font-medium text-sm transition-colors ${
                  activeTab === 'saved'
                    ? 'border-blue-500 text-blue-500'
                    : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
              >
                <div className="flex items-center space-x-2">
                  <Bookmark size={16} />
                  <span>좋아요 한 게시물</span>
                </div>
              </button>
            </div>
          </div>

          {/* 게시물 그리드 */}
          <div>
            {activeTab === 'posts' ? (
              <div className="grid grid-cols-3 gap-4">
                {posts.map((post) => (
                  <div key={post.id} className="relative aspect-square bg-gray-100 rounded-lg overflow-hidden cursor-pointer group">
                    <img
                      src={post.imageUrl}
                      alt={`Post ${post.id}`}
                      className="w-full h-full object-cover transition-transform group-hover:scale-105 duration-300"
                    />

                    {/* 비디오 표시 */}
                    {post.type === 'video' && (
                      <div className="absolute top-2 right-2">
                        <div className="bg-black bg-opacity-60 rounded-full p-1">
                          <Play size={14} className="text-white fill-current" />
                        </div>
                      </div>
                    )}

                    {/* 호버 오버레이 */}
                    <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 transition-all duration-300 flex items-center justify-center">
                      <div className="opacity-0 group-hover:opacity-100 flex space-x-4 text-white transition-all duration-300">
                        <div className="flex items-center space-x-1">
                          <Heart size={16} className="fill-current" />
                          <span className="text-sm font-medium">{formatNumber(post.likeCount)}</span>
                        </div>
                        <div className="flex items-center space-x-1">
                          <MessageCircle size={16} />
                          <span className="text-sm font-medium">{formatNumber(post.commentCount)}</span>
                        </div>
                        <div className="flex items-center space-x-1">
                          <Eye size={16} />
                          <span className="text-sm font-medium">{formatNumber(post.viewCount)}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-12">
                <Heart size={48} className="text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">좋아요 한 게시물이 없습니다</p>
              </div>
            )}
          </div>
        </div>
      </Card>

      <Dialog open={isEditModalOpen} onOpenChange={setIsEditModalOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>프로필 수정</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <label htmlFor="displayName" className="text-right">이름</label>
              <input
                id="displayName"
                value={formData.displayName}
                onChange={(e) => setFormData({ ...formData, displayName: e.target.value })}
                className="col-span-3 p-2 border rounded"
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <label htmlFor="intro" className="text-right">소개</label>
              <textarea
                id="intro"
                value={formData.intro}
                onChange={(e) => setFormData({ ...formData, intro: e.target.value })}
                className="col-span-3 p-2 border rounded"
                rows={4}
              />
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <label htmlFor="profileImage" className="text-right">프로필 사진</label>
              <div className="col-span-3 flex items-center space-x-2">
                <input
                  id="profileImage"
                  type="file"
                  accept="image/*"
                  onChange={(e) => setFormData({ ...formData, profileImage: e.target.files[0] })}
                  className="p-2 border rounded"
                />
                {formData.profileImage && (
                  <button
                    onClick={() => handleDeleteImage('profileImage')}
                    className="p-2 bg-red-500 text-white rounded hover:bg-red-600"
                  >
                    <Trash2 size={16} />
                  </button>
                )}
              </div>
              {formData.profileImage && (
                <img
                  src={typeof formData.profileImage === 'string' ? formData.profileImage : URL.createObjectURL(formData.profileImage)}
                  alt="Preview"
                  className="col-span-3 mt-2 h-24 object-cover rounded"
                />
              )}
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <label htmlFor="backgroundImage" className="text-right">배경 사진</label>
              <div className="col-span-3 flex items-center space-x-2">
                <input
                  id="backgroundImage"
                  type="file"
                  accept="image/*"
                  onChange={(e) => setFormData({ ...formData, backgroundImage: e.target.files[0] })}
                  className="p-2 border rounded"
                />
                {formData.backgroundImage && (
                  <button
                    onClick={() => handleDeleteImage('backgroundImage')}
                    className="p-2 bg-red-500 text-white rounded hover:bg-red-600"
                  >
                    <Trash2 size={16} />
                  </button>
                )}
              </div>
              {formData.backgroundImage && (
                <img
                  src={typeof formData.backgroundImage === 'string' ? formData.backgroundImage : URL.createObjectURL(formData.backgroundImage)}
                  alt="Preview"
                  className="col-span-3 mt-2 h-24 object-cover rounded"
                />
              )}
            </div>
          </div>
          <DialogFooter>
            <button
              onClick={() => setIsEditModalOpen(false)}
              className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
            >
              취소
            </button>
            <button
              onClick={handleSaveProfile}
              className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              저장
            </button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default UserProfileCard;
