package com.sorry.personalpoi.view;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * @anthor sorry
 * @time 2019/5/17
 * 提供回调的接口

 */

public abstract class VideoPlayerListener implements IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnSeekCompleteListener {
}
