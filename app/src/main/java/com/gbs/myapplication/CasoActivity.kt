package com.gbs.myapplication

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar


class CasoActivity : AppCompatActivity() {

    private lateinit var editTextDniRuc: TextInputEditText
    private lateinit var editTextNombresApellidos: TextInputEditText
    private lateinit var editTextCorreoElectronico: TextInputEditText
    private lateinit var editTextTelefono: TextInputEditText
    private lateinit var editTextDireccion: TextInputEditText
    private lateinit var editTextFechaCompra: TextInputEditText
    private lateinit var radioGroupTipoDocumento: RadioGroup
    private lateinit var radioGroupTipoCaso: RadioGroup
    private lateinit var editTextNombreEquipo: TextInputEditText
    private lateinit var btnAdjuntarArchivo: Button
    private lateinit var btnEnviarReporte: Button
    private lateinit var imagePreview: ImageView

    private var archivoAdjuntoUri: Uri? = null


    private val seleccionarArchivoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            archivoAdjuntoUri = result.data?.data
            archivoAdjuntoUri?.let {
                Glide.with(this).load(it).into(imagePreview)
                imagePreview.visibility = View.VISIBLE  // Asegura que el ImageView sea visible después de cargar la imagen

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caso)

        // Inicializar vistas
        editTextDniRuc = findViewById(R.id.editTextDniRuc)
        editTextNombresApellidos = findViewById(R.id.editTextNombresApellidos)
        editTextCorreoElectronico = findViewById(R.id.editTextCorreoElectronico)
        editTextTelefono = findViewById(R.id.editTextTelefono)
        editTextDireccion = findViewById(R.id.editTextDireccion)
        editTextFechaCompra = findViewById(R.id.editTextFechaCompra)
        radioGroupTipoDocumento = findViewById(R.id.radioGroupTipoDocumento)
        radioGroupTipoCaso = findViewById(R.id.radioGroupTipoCaso)
        editTextNombreEquipo = findViewById(R.id.editTextNombreEquipo)
        btnAdjuntarArchivo = findViewById(R.id.btnAdjuntarArchivo)
        btnEnviarReporte = findViewById(R.id.btnEnviarReporte)
        imagePreview = findViewById(R.id.imagePreview)

        // Solicitar permisos de almacenamiento
        solicitarPermisos()

        btnAdjuntarArchivo.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    seleccionarArchivo()
                } else {
                    // Verificar si el usuario ha denegado los permisos previamente
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Mostrar un mensaje explicativo al usuario
                        Toast.makeText(this, "Es necesario permitir el acceso al almacenamiento para adjuntar archivos", Toast.LENGTH_SHORT).show()
                    } else {
                        // Informar al usuario que debe habilitar los permisos desde la configuración de la aplicación
                        Toast.makeText(this, "Debe habilitar los permisos desde la configuración de la aplicación", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                seleccionarArchivo()
            }
        }


        btnEnviarReporte.setOnClickListener {
            if (validarCampos()) {
                enviarReporteWhatsApp()
            }
        }

        // Configurar el DatePickerDialog para el campo de texto de la fecha de compra
        editTextFechaCompra.isFocusable = false
        editTextFechaCompra.isClickable = true
        editTextFechaCompra.setOnClickListener {
            mostrarDatePickerDialog()
        }
        // Configurar TextWatchers para contar los caracteres y aplicar límites dinámicos
        setupTextWatchers()

    }

    private fun solicitarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1)
        } else {
            seleccionarArchivo()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permisos concedidos
            } else {
                Toast.makeText(this, "Permisos de almacenamiento son necesarios para seleccionar archivos", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupTextWatchers() {
        // TextViews para mostrar la cantidad de caracteres actuales
        val textViewDniRucCount = findViewById<TextView>(R.id.textViewDniRucCount)
        val textViewNombresApellidosCount = findViewById<TextView>(R.id.textViewNombresApellidosCount)
        val textViewCorreoElectronicoCount = findViewById<TextView>(R.id.textViewCorreoElectronicoCount)
        val textViewTelefonoCount =findViewById<TextView>(R.id.textViewTelefonoCount)
        val textViewDireccionCount = findViewById<TextView>(R.id.textViewDireccionCount)
        val textViewFechaCompraCount = findViewById<TextView>(R.id.textViewFechaCompraCount)
        val textViewNombreEquipoCount = findViewById<TextView>(R.id.textViewNombreEquipoCount)
        // TextWatcher para el campo DNI/RUC
        editTextDniRuc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val currentLength = it.length
                    val maxLength = 11 // Ajustar según el límite requerido
                    if (currentLength > maxLength) {
                        editTextDniRuc.error = "Límite de caracteres excedido"
                    } else {
                        editTextDniRuc.error = null
                    }

                    // Mostrar la cantidad de caracteres actual en el textViewDniRucCount
                    val caracteresEscritos = "$currentLength / $maxLength"
                    textViewDniRucCount.text = caracteresEscritos
                }
            }
        })

        // TextWatcher para el campo Nombres y Apellidos
        editTextNombresApellidos.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val currentLength = it.length
                    val maxLength = 40 // Ajustar según el límite requerido
                    if (currentLength > maxLength) {
                        editTextNombresApellidos.error = "Límite de caracteres excedido"
                    } else {
                        editTextNombresApellidos.error = null
                    }

                    // Mostrar la cantidad de caracteres actual en el textViewNombresApellidosCount
                    val caracteresEscritos = "$currentLength / $maxLength"
                    textViewNombresApellidosCount.text = caracteresEscritos
                }
            }
        })

        editTextCorreoElectronico.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val currentLength = it.length
                    val maxLength = 100 // Ajustar según el límite requerido
                    if (currentLength > maxLength) {
                        editTextCorreoElectronico.error = "Límite de caracteres excedido"
                    } else {
                        editTextCorreoElectronico.error = null
                    }

                    // Mostrar la cantidad de caracteres actual en el textViewNombresApellidosCount
                    val caracteresEscritos = "$currentLength / $maxLength"
                    textViewCorreoElectronicoCount.text = caracteresEscritos
                }
            }
        })

        editTextTelefono.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val currentLength = it.length
                    val maxLength = 9 // Ajustar según el límite requerido
                    if (currentLength > maxLength) {
                        editTextTelefono.error = "Límite de caracteres excedido"
                    } else {
                        editTextTelefono.error = null
                    }

                    // Mostrar la cantidad de caracteres actual en el textViewNombresApellidosCount
                    val caracteresEscritos = "$currentLength / $maxLength"
                    textViewTelefonoCount.text = caracteresEscritos
                }
            }
        })

        editTextDireccion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val currentLength = it.length
                    val maxLength = 400 // Ajustar según el límite requerido
                    if (currentLength > maxLength) {
                        editTextDireccion.error = "Límite de caracteres excedido"
                    } else {
                        editTextDireccion.error = null
                    }

                    // Mostrar la cantidad de caracteres actual en el textViewNombresApellidosCount
                    val caracteresEscritos = "$currentLength / $maxLength"
                    textViewDireccionCount.text = caracteresEscritos
                }
            }
        })

        editTextFechaCompra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val currentLength = it.length
                    val maxLength = 10 // Ajustar según el límite requerido
                    if (currentLength > maxLength) {
                        editTextFechaCompra.error = "Límite de caracteres excedido"
                    } else {
                        editTextFechaCompra.error = null
                    }

                    // Mostrar la cantidad de caracteres actual en el textViewNombresApellidosCount
                    val caracteresEscritos = "$currentLength / $maxLength"
                    textViewFechaCompraCount.text = caracteresEscritos
                }
            }
        })

        editTextNombreEquipo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val currentLength = it.length
                    val maxLength = 50 // Ajustar según el límite requerido
                    if (currentLength > maxLength) {
                        editTextNombreEquipo.error = "Límite de caracteres excedido"
                    } else {
                        editTextNombreEquipo.error = null
                    }

                    // Mostrar la cantidad de caracteres actual en el textViewNombresApellidosCount
                    val caracteresEscritos = "$currentLength / $maxLength"
                    textViewNombreEquipoCount.text = caracteresEscritos
                }
            }
        })

    }


    private fun mostrarDatePickerDialog() {
        val calendario = Calendar.getInstance()
        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val fechaSeleccionada = "$dayOfMonth/${monthOfYear + 1}/$year"
                editTextFechaCompra.setText(fechaSeleccionada)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()



    }



    private fun validarCampos(): Boolean {
        var camposValidos = true

        val dniRuc = editTextDniRuc.text.toString()
        val nombresApellidos = editTextNombresApellidos.text.toString()
        val correoElectronico = editTextCorreoElectronico.text.toString()
        val telefono = editTextTelefono.text.toString()
        val direccion = editTextDireccion.text.toString()
        val fechaCompra = editTextFechaCompra.text.toString()
        val nombreEquipo = editTextNombreEquipo.text.toString()

        // Validación del DNI/RUC como números
        if (!dniRuc.matches("\\d{8}|\\d{11}".toRegex())) {
            editTextDniRuc.error = "Debe contener solo números de 8 o 11 caracteres"
            camposValidos = false
        } else {
            editTextDniRuc.error = null
        }


        // Validación de Nombres y Apellidos como letras y no vacíos
        if (nombresApellidos.isEmpty()) {
            editTextNombresApellidos.error = "Este campo no puede estar vacío"
            camposValidos = false
        } else if (!nombresApellidos.matches("[a-zA-Z ]+".toRegex())) {
            editTextNombresApellidos.error = "Debe contener solo letras"
            camposValidos = false
        } else {
            editTextNombresApellidos.error = null
        }

        // Validación del Teléfono como números
        if (!telefono.matches("\\d{9}".toRegex())) {
            editTextTelefono.error = "Debe contener solo números de 9 caracteres"
            camposValidos = false
        } else {
            editTextTelefono.error = null
        }

        // Validación de Dirección como no vacía
        if (direccion.isEmpty()) {
            editTextDireccion.error = "Este campo no puede estar vacío"
            camposValidos = false
        } else {
            editTextDireccion.error = null
        }

        // Validación del Nombre del Equipo como letras
        if (!nombreEquipo.matches("[a-zA-Z ]+".toRegex())) {
            editTextNombreEquipo.error = "Debe contener solo letras"
            camposValidos = false
        } else {
            editTextNombreEquipo.error = null
        }

        // Validación del Correo Electrónico
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoElectronico).matches()) {
            editTextCorreoElectronico.error = "Correo electrónico inválido"
            camposValidos = false
        } else {
            editTextCorreoElectronico.error = null
        }

        // Validación de la Fecha de Compra como no vacía
        if (fechaCompra.isEmpty()) {
            editTextFechaCompra.error = "Este campo no puede estar vacío"
            camposValidos = false
        } else {
            editTextFechaCompra.error = null
        }

        // Validación de la selección de archivo adjunto
        if (archivoAdjuntoUri == null) {
            showToast("Debe adjuntar una imagen o video")
            camposValidos = false
        }

        // Validación de Tipo de Documento seleccionado
        if (radioGroupTipoDocumento.checkedRadioButtonId == -1) {
            showToast("Debe seleccionar un tipo de documento")
            camposValidos = false
        }

        // Validación de Tipo de Caso seleccionado
        if (radioGroupTipoCaso.checkedRadioButtonId == -1) {
            showToast("Debe seleccionar un tipo de caso")
            camposValidos = false
        }

        return camposValidos
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun seleccionarArchivo() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        seleccionarArchivoLauncher.launch(intent)
    }


    private fun enviarReporteWhatsApp() {
        val dniRuc = editTextDniRuc.text.toString()
        val nombresApellidos = editTextNombresApellidos.text.toString()
        val correoElectronico = editTextCorreoElectronico.text.toString()
        val telefono = editTextTelefono.text.toString()
        val direccion = editTextDireccion.text.toString()
        val fechaCompra = editTextFechaCompra.text.toString()
        val nombreEquipo = editTextNombreEquipo.text.toString()

        val tipoDocumento = when (radioGroupTipoDocumento.checkedRadioButtonId) {
            R.id.radioButtonBoleta -> "Boleta"
            R.id.radioButtonFactura -> "Factura"
            else -> ""
        }

        val tipoCaso = when (radioGroupTipoCaso.checkedRadioButtonId) {
            R.id.radioButtonReparacion -> "Reparación"
            R.id.radioButtonMantenimiento -> "Mantenimiento"
            R.id.radioButtonGarantia -> "Garantía"
            else -> ""
        }

        val mensaje = """
                *DATOS DEL CLIENTE / TÉCNICO*
                DNI/RUC: $dniRuc
                Nombres y Apellidos: $nombresApellidos
                Correo Electrónico: $correoElectronico
                Teléfono: $telefono
                Dirección: $direccion
                
                *DATOS DEL COMPROBANTE DE COMPRA*
                Fecha de Compra: $fechaCompra
                Tipo de Documento: $tipoDocumento
                
                *TIPO DE CASO*
                $tipoCaso
                
                *DATOS DEL EQUIPO*
                Nombre del Equipo: $nombreEquipo
            """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, mensaje)
            type = "text/plain"
        }

        archivoAdjuntoUri?.let { uri ->
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.type = contentResolver.getType(uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(intent, "Enviar reporte con")

        try {
            startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se encontró una aplicación para enviar el reporte", Toast.LENGTH_SHORT).show()
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            archivoAdjuntoUri = data?.data
            archivoAdjuntoUri?.let { uri ->
                // Mostrar la vista previa de la imagen o video adjunto usando Glide
                imagePreview.visibility = View.VISIBLE
                Glide.with(this)
                    .load(uri)
                    .into(imagePreview)
            }
        }
    }
}