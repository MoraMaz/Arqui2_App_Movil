package moramaz.com.smartstick;

public class Recorrido {

	private String IDSesion, Longitud, Latitud, TipoObstaculo, IMEI, IDCoach, IDPaciente;

	public Recorrido(){
		IDSesion = "";
		Longitud = "";
		Latitud = "";
		TipoObstaculo = "";
		IMEI = "";
		IDCoach = "";
		IDPaciente = "";
	}


	public String getIDSesion() {
		return IDSesion;
	}

	public void setIDSesion(String IDSesion) {
		this.IDSesion = IDSesion;
	}

	public String getLongitud() {
		return Longitud;
	}

	public void setLongitud(String longitud) {
		Longitud = longitud;
	}

	public String getLatitud() {
		return Latitud;
	}

	public void setLatitud(String latitud) {
		Latitud = latitud;
	}

	public String getTipoObstaculo() {
		return TipoObstaculo;
	}

	public void setTipoObstaculo(String tipoObstaculo) {
		TipoObstaculo = tipoObstaculo;
	}

	public String getIMEI() {
		return IMEI;
	}

	public void setIMEI(String IMEI) {
		this.IMEI = IMEI;
	}

	public String getIDCoach() {
		return IDCoach;
	}

	public void setIDCoach(String IDCoach) {
		this.IDCoach = IDCoach;
	}

	public String getIDPaciente() {
		return IDPaciente;
	}

	public void setIDPaciente(String IDPaciente) {
		this.IDPaciente = IDPaciente;
	}
}