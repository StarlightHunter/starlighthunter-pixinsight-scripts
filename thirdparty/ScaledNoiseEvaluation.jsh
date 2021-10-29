// ----------------------------------------------------------------------------
// PixInsight JavaScript Runtime API - PJSR Version 1.0
// ----------------------------------------------------------------------------
// NoiseEvaluation.js - Released 2021-05-31T07:25:09Z
// ----------------------------------------------------------------------------
//
// This file contains the ScaledNoiseEvaluation function of PixInsight Noise
// Evaluation Script version 2.1.1
//
// Copyright (c) 2006-2021 Pleiades Astrophoto S.L.
//
// Redistribution and use in both source and binary forms, with or without
// modification, is permitted provided that the following conditions are met:
//
// 1. All redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//
// 2. All redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// 3. Neither the names "PixInsight" and "Pleiades Astrophoto", nor the names
//    of their contributors, may be used to endorse or promote products derived
//    from this software without specific prior written permission. For written
//    permission, please contact info@pixinsight.com.
//
// 4. All products derived from this software, in any form whatsoever, must
//    reproduce the following acknowledgment in the end-user documentation
//    and/or other materials provided with the product:
//
//    "This product is based on software from the PixInsight project, developed
//    by Pleiades Astrophoto and its contributors (https://pixinsight.com/)."
//
//    Alternatively, if that is where third-party acknowledgments normally
//    appear, this acknowledgment must be reproduced in the product itself.
//
// THIS SOFTWARE IS PROVIDED BY PLEIADES ASTROPHOTO AND ITS CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL PLEIADES ASTROPHOTO OR ITS
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, BUSINESS
// INTERRUPTION; PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; AND LOSS OF USE,
// DATA OR PROFITS) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
// ----------------------------------------------------------------------------

/**
 * Estimation of the standard deviation of the noise, assuming a Gaussian
 * noise distribution.
 *
 * - Use MRS noise evaluation when the algorithm converges for 4 >= J >= 2
 *
 * - Use k-sigma noise evaluation when either MRS doesn't converge or the
 *   length of the noise pixels set is below a 1% of the image area.
 *
 * - Automatically iterate to find the highest layer where noise can be
 *   successfully evaluated, in the [1,3] range.
 *
 * Returned noise estimates are scaled by the Sn robust scale estimator of
 * Rousseeuw and Croux.
 *
 * Copyright (C) 2006-2021 Pleiades Astrophoto. All Rights Reserved.
 * Written by Juan Conejero (PTeam)
 */
function ScaledNoiseEvaluation( image )
{
   let scale = image.Sn();
   if ( 1 + scale == 1 )
      throw Error( "Zero or insignificant data." );

   let a, n = 4, m = 0.01*image.selectedRect.area;
   for ( ;; )
   {
      a = image.noiseMRS( n );
      if ( a[1] >= m )
         break;
      if ( --n == 1 )
      {
         console.writeln( "<end><cbr>** Warning: No convergence in MRS noise evaluation routine - using k-sigma noise estimate." );
         a = image.noiseKSigma();
         break;
      }
   }
   this.sigma = a[0]/scale; // estimated scaled stddev of Gaussian noise
   this.count = a[1];       // number of pixels in the noisy pixels set
   this.layers = n;         // number of layers used for noise evaluation
}
