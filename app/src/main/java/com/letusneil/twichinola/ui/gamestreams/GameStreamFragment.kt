package com.letusneil.twichinola.ui.gamestreams

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.letusneil.twichinola.R
import com.letusneil.twichinola.data.GameStream
import com.letusneil.twichinola.data.Stream
import com.letusneil.twichinola.databinding.GameStreamsFragmentBinding
import com.letusneil.twichinola.di.Twichinola
import com.letusneil.twichinola.util.autoCleared
import timber.log.Timber
import javax.inject.Inject

class GameStreamFragment : Fragment(R.layout.game_streams_fragment) {

  @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

  private var binding by autoCleared<GameStreamsFragmentBinding>()

  private val viewModel: GameStreamViewModel by viewModels { viewModelFactory }

  private val args: GameStreamFragmentArgs by navArgs()

  override fun onAttach(context: Context) {
    Twichinola.dependencyInjector().inject(this)
    super.onAttach(context)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding = GameStreamsFragmentBinding.bind(view)
    val gameName = args.gameName

    binding.streamsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val lastPosition = layoutManager.findLastVisibleItemPosition()
        if (lastPosition == viewModel.streams.size - 1) {
          viewModel.getGameStreams(gameName)
        }
      }
    })

    viewModel.viewEvent.observe(viewLifecycleOwner, Observer {
      when (it) {
        is GameStreamsUIState.Successful -> setStreams(it.streams)
        is GameStreamsUIState.Loading -> Timber.d("Loading")
        is GameStreamsUIState.Error -> Timber.d("Error")
      }
    })

    viewModel.getGameStreams(gameName)
  }

  private fun setStreams(streams: List<Stream>) {
    binding.streamsList.withModels {
      streams.forEach { stream ->
        with(stream) {
          gameStreamsEpoxyHolder {
            id("stream_id_${channel.name}")
            channelName(channel.name)
            gameName(game)
            streamerName(channel.display_name)
            streamPreviewImageUrl(preview.template)
            streamerImageUrl(channel.logo)
            streamDescription(channel.status)
            streamType(stream_type)
            streamLanguage(channel.broadcaster_language)
            viewersCount(viewers)

            listener { stream, extras ->
              findNavController().navigate(
                GameStreamFragmentDirections.toPlayerFragment(stream), extras
              )
            }
          }
        }
      }
    }
  }
}