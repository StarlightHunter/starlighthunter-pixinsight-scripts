/*
-------------------------------------------------------------------------------
StarlightHunter.com Automatic Linear Fitting script
-------------------------------------------------------------------------------

MIT License

Copyright (c) 2021 Oliver Gutiérrez - StarlightHunter.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

-------------------------------------------------------------------------------
*/

#feature-id StarlightHunter.com Scripts > Automatic Linear Fitting
#feature-info  This script does an automatic linear fitting for RGB images

#include "../utils.jsh"

// Make use of the ScaledNoiseEvaluation function from NoiseEvaluation script
#include "../thirdparty/ScaledNoiseEvaluation.jsh"


function auto_linear_fit(view) {
   print_message("Calculating noise of each channel");

   let channel_noises = [];
   let lowest_noise_c = 0;

   for ( let c = 0; c < view.image.numberOfChannels; ++c )
   {
      view.image.selectedChannel = c;
      let E = new ScaledNoiseEvaluation( view.image );
      channel_noises[c] = E.sigma;
      if (c != 0)
      {
         if (channel_noises[lowest_noise_c] > E.sigma)
         {
            lowest_noise_c = c;
         }
      }
      print_submessage("Channel " + c.toString() + ": " + E.sigma.toString());
   }

   print_message("Lowest noise channel: " + lowest_noise_c.toString());

   print_message("Extracting RGB channels");
   let channel_views = extract_rgb_channels(view);

   print_message("Linear fitting channels");
   for ( let c = 0; c < channel_views.length; ++c )
   {
      if (c != lowest_noise_c)
      {
         print_submessage("Linear fitting channel " + c.toString() + " against channel " + lowest_noise_c.toString());
         do_linear_fit(channel_views[c], channel_views[lowest_noise_c])
      }
   }

   print_message("Composing linear fitted image");

   let newWinView = create_window_for_image(view.image, view.id + "_linearfit", true);

   // Recompose RGB image using pixel math
   do_multiexpression_pixel_math(
      newWinView,
      channel_views[0].id,
      channel_views[1].id,
      channel_views[2].id
   )

   // Close all intermediate image windows
   for ( let c = 0; c < channel_views.length; ++c )
   {
      ImageWindow.windowById(channel_views[c].id).forceClose();
   }
   return newWinView;
}

function main() {
   Console.show();

   print_header("Starting Automatic Linear fitting script");

   // Check there is an active window
   let window = ImageWindow.activeWindow;
   if ( window.isNull )
      throw new Error( "No active image" );

   // Do automatic linear fitting
   let new_window = auto_linear_fit(window.currentView);

   Console.writeln("* New image: ", new_window.id);

   print_footer("Finished Automatic Linear fitting script");
}

main();
