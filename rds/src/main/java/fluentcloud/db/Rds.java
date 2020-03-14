package fluentcloud.db;

import com.google.common.net.HostAndPort;

import io.reactivex.rxjava3.core.Flowable;
import software.amazon.awssdk.regions.Region;

public class Rds {

  /**
   * periodically checks the current set of RDS endpoints.
   */

  public static Flowable<HostAndPort> ports() {
    return Flowable.empty();
  }

  /**
   * provides a token which is valid for connecting to the given instance.
   */

  public static Flowable<String> credentials(Region region, HostAndPort endpoint, String username) {
    return Flowable.never();
  }

}
