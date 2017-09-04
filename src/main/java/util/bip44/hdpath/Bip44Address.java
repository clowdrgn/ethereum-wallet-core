package util.bip44.hdpath;

import com.google.common.primitives.UnsignedInteger;
import util.bip44.hdpath.Bip44Chain;
import util.bip44.hdpath.HdKeyPath;


public class Bip44Address extends Bip44Chain {
   public Bip44Address(Bip44Chain parent, UnsignedInteger index, boolean hardened) {
      super(parent, index, hardened);
   }

   @Override
   protected HdKeyPath knownChildFactory(UnsignedInteger index, boolean hardened) {
      throw new RuntimeException("Bip44 allows no childs below addresses");
   }
}
