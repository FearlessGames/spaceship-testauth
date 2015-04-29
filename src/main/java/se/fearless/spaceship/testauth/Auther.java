package se.fearless.spaceship.testauth;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.AbstractHttpContentHolder;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import se.fearless.common.json.GsonSerializer;
import se.fearless.common.json.JsonSerializer;
import se.fearless.common.security.BCrypter;
import se.fearless.common.security.Digester;

import java.nio.charset.Charset;

public class Auther {

	public static final String BASE_PATH = "http://localhost:8080/api/public/users/";
	private static JsonSerializer jsonSerializer = new GsonSerializer();;
	private static Charset charset = Charset.forName("UTF-8");;


	public static void main(String[] args) {
		String userName = "foobar";
		String password = "12345";
		String feature = "spaceship";

		Observable<SaltData> saltData = askFameForSalt(userName);

		Observable<String> hash = hashUserNameAndPasswordUsingSalt(userName, password, saltData);


		Observable<String> authToken = authUserUsingHash(userName, hash);

		String first = authToken.toBlocking().first();
        System.out.println(first);
        Observable<String> sessionKey = loginToSpaceShipUsingAuthToken(authToken);

	}

	private static Observable<SaltData> askFameForSalt(String userName) {
        String uri = BASE_PATH + userName + "/salt";
        return getDataFromFame(uri,  SaltData.class);
	}



    private static Observable<String> hashUserNameAndPasswordUsingSalt(final String userName, String password, Observable<SaltData> saltDataObservable) {

		final Digester digester = new Digester("beefcake");
		return saltDataObservable.map(saltData-> {
            System.out.println("UserSalt: " + saltData.getUserSalt());
            System.out.println("OneTimeSalt: " + saltData.getOneTimeSalt());
            String userHash = BCrypter.bcrypt(userName+password, saltData.getUserSalt());
			return digester.sha512Hex(userHash + saltData.getOneTimeSalt());
		});
	}

	private static Observable<String> authUserUsingHash(String userName, Observable<String> hashObservable) {

		Observable<Observable<String>> observableObservable = hashObservable.map(hash -> {
            String uri = BASE_PATH + userName + "/auth/spaceship/" + hash;
            Observable<FameToken> fameTokenObservable = getDataFromFame(uri, FameToken.class);
			return fameTokenObservable.map(fameToken -> fameToken.token);
		});

		return observableObservable.flatMap(stringObservable -> stringObservable);

	}



	private static Observable<String> loginToSpaceShipUsingAuthToken(Observable<String> authToken) {
		return null;
	}


	private static class FameToken {
		String token;
	}


    private static <T> Observable<T> getDataFromFame(String uri, Class<T> dataType) {
        Observable<HttpClientResponse<ByteBuf>> httpClientResponseObservable = RxNetty.createHttpGet(uri);
        Observable<ByteBuf> byteBufObservable = httpClientResponseObservable.flatMap(AbstractHttpContentHolder::getContent);
        Observable<String> stringObservable = byteBufObservable.map(byteBuf1 -> byteBuf1.toString(charset));
        return stringObservable.map(string -> {
            System.out.println("fetched Json string: " + string + " of type " + dataType.toString());
            return jsonSerializer.fromJson(dataType, string);


        });
    }

}
