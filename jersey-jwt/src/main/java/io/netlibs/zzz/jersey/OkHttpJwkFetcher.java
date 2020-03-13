package io.netlibs.zzz.jersey;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.auth0.jwk.Jwk;
import com.google.common.io.Files;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpJwkFetcher implements JwkFetcher {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OkHttpJwkFetcher.class);
  private final WellKnownJwkTransformer transformer = new WellKnownJwkTransformer();
  private final OkHttpClient client;
  private final JwkAuthorityResolver resolver;

  public OkHttpJwkFetcher(Path cacheDir, JwkAuthorityResolver resolver) {
    this(standardClient(cacheDir), resolver);
  }

  public OkHttpJwkFetcher(JwkAuthorityResolver resolver) {
    this(standardClient(), resolver);
  }

  public static OkHttpClient standardClient() {
    OkHttpClient.Builder b = new OkHttpClient.Builder();
    b.connectTimeout(5, TimeUnit.SECONDS);
    b.writeTimeout(5, TimeUnit.SECONDS);
    b.readTimeout(5, TimeUnit.SECONDS);
    b.callTimeout(5, TimeUnit.SECONDS);
    b.followSslRedirects(false);
    return b.build();
  }

  public static OkHttpClient standardClient(Path cacheDir) {
    OkHttpClient.Builder b = new OkHttpClient.Builder();
    b.connectTimeout(5, TimeUnit.SECONDS);
    b.writeTimeout(5, TimeUnit.SECONDS);
    b.readTimeout(5, TimeUnit.SECONDS);
    b.callTimeout(5, TimeUnit.SECONDS);
    b.followSslRedirects(false);
    File directory = Files.createTempDir();
    b.cache(new Cache(directory, 1024 * 1024 * 32));
    log.debug("HTTP cache at {}", directory);
    return b.build();
  }

  public OkHttpJwkFetcher(OkHttpClient client, JwkAuthorityResolver resolver) {
    this.client = client;
    this.resolver = resolver;
  }

  public OkHttpJwkFetcher() {
    this(new JwkWellKnownResolver());
  }

  public CompletableFuture<Void> precache(String host) {
    URL url;
    try {
      url = new URL("https", host, "/.well-known/jwks.json");
    }
    catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
    return get(url).thenAccept(res -> log.debug(res.toString()));
  }

  @Override
  public CompletableFuture<Optional<Jwk>> fetch(JwkKeyId keyId) {

    URL url = resolver.resolve(keyId);

    if (url == null) {
      return CompletableFuture
        .failedFuture(new IllegalArgumentException("unable to resolve authority " + keyId.authority()));
    }

    return get(url).thenApply(doc -> Optional.ofNullable(transformer.transform(doc, keyId)));

  }

  private CompletableFuture<Response> get(URL url) {

    Request request =
      new Request.Builder().url(url)
        .addHeader("accept", "application/json")
        .cacheControl(new CacheControl.Builder()
          .maxStale(1, TimeUnit.HOURS)
          .build())
        .build();

    Call call = client.newCall(request);

    CompletableFuture<Response> res = new CompletableFuture<>();

    call.enqueue(new Callback() {

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        try {
          res.complete(response);
        }
        catch (Exception ex) {
          res.completeExceptionally(ex);
        }
        finally {
          response.close();
        }
      }

      @Override
      public void onFailure(Call call, IOException e) {
        res.completeExceptionally(e);
      }

    });

    return res;
  }

}
