# VideoPlayerSampleapp
## 비디오 샘플 앱입니다. 

Android Studio + Java로 작성되었습니다. 사용된 라이브러리는 다음과 같습니다.

1.ExoPlayer

2.Material-dialogs

3.Android Annotations 

4.CircleImageView 

***
김인강 초기 소스코드 적용본입니다.

1. ExoPlayerView를 커스터마이즈했습니다. 이제 ExoPlayerControlView에서 화면을 확대할 수 있습니다(Portrait->Horizontal, Horizontal -> Portait)

2. 속도 조절과 화면 고정을 추가했습니다.(속도 조절은 API 23 이상에서만 적용) 

3. 상단에 동영상이 재생되고, 하단에는 추가정보가 나오도록 UI를 구성했습니다.

4. 1분 미리보기가 끝나면,구매 유도 팝업이 뜨고 그에 따라 긴 영상으로 갈 수 있도록 조정했습니다. 

***

##추가 구현 목표 

1. 볼륨 조절과 화면 고정 버튼이 현 ExoplayerControlView처럼 천천히 사라졌다가 클릭시만 보였으면 좋겠다. (View focus 문제가 해결이 안됨)

2. 다른 소리가 재생중에는 영상을 켰을때 focus를 가져왔으면 좋겠다.

3. +)
